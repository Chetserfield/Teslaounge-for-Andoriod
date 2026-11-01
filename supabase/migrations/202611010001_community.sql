begin;
create table public.profiles (
 id uuid primary key references auth.users(id) on delete cascade,
 nickname text not null check (char_length(btrim(nickname)) between 1 and 20),
 audience smallint not null default 0 check (audience between 0 and 2),
 created_at timestamptz not null default now()
);
create table public.posts (
 id uuid primary key default gen_random_uuid(),
 author_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
 title text not null check (char_length(btrim(title)) between 1 and 80),
 body text not null check (char_length(btrim(body)) between 1 and 3000),
 category text not null check (category in ('구매·인도 Q&A','오너 생활','지역 모임')),
 hidden boolean not null default false,
 created_at timestamptz not null default now()
);
create table public.comments (
 id uuid primary key default gen_random_uuid(),
 post_id uuid not null references public.posts(id) on delete cascade,
 author_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
 body text not null check (char_length(btrim(body)) between 1 and 500),
 hidden boolean not null default false,
 created_at timestamptz not null default now()
);
create table public.reactions (
 post_id uuid not null references public.posts(id) on delete cascade,
 user_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
 primary key (post_id,user_id)
);
create table public.providers (
 id uuid primary key default gen_random_uuid(),
 name text not null check (char_length(btrim(name)) between 1 and 80),
 region text not null check (char_length(btrim(region)) between 1 and 100),
 category text not null check (category in ('틴팅','PPF','신차패키지','세차','사고수리')),
 description text not null default '' check (char_length(description)<=2000),
 approved boolean not null default false,
 created_at timestamptz not null default now()
);
create table public.advertisements (
 id uuid primary key default gen_random_uuid(),
 provider_id uuid not null references public.providers(id) on delete cascade,
 title text not null check (char_length(btrim(title)) between 1 and 80),
 description text not null default '' check (char_length(description)<=500),
 approved boolean not null default false,
 starts_at timestamptz not null,
 ends_at timestamptz not null check (ends_at>starts_at)
);
create index posts_created_idx on public.posts(created_at desc,id);
create index posts_author_idx on public.posts(author_id);
create index comments_post_idx on public.comments(post_id,created_at);
create index comments_author_idx on public.comments(author_id);
create index reactions_user_idx on public.reactions(user_id);
create index advertisements_provider_idx on public.advertisements(provider_id);

alter table public.profiles enable row level security;
alter table public.posts enable row level security;
alter table public.comments enable row level security;
alter table public.reactions enable row level security;
alter table public.providers enable row level security;
alter table public.advertisements enable row level security;

-- Explicit privileges also protect moderation and ownership columns.
revoke all on public.profiles,public.posts,public.comments,public.reactions,public.providers,public.advertisements from public,anon,authenticated;
grant select on public.profiles,public.posts,public.comments,public.providers,public.advertisements to anon,authenticated;
grant select on public.reactions to authenticated;
grant insert(id,nickname,audience),update(nickname,audience) on public.profiles to authenticated;
grant insert(id,title,body,category),update(title,body,category),delete on public.posts to authenticated;
grant insert(id,post_id,body),update(body),delete on public.comments to authenticated;
grant insert(post_id),delete on public.reactions to authenticated;

create policy profiles_read on public.profiles for select using(true);
create policy profiles_insert on public.profiles for insert to authenticated with check(id=(select auth.uid()));
create policy profiles_update on public.profiles for update to authenticated using(id=(select auth.uid())) with check(id=(select auth.uid()));
create policy posts_read on public.posts for select using(not hidden);
create policy posts_insert on public.posts for insert to authenticated with check(author_id=(select auth.uid()) and not hidden);
create policy posts_update on public.posts for update to authenticated using(author_id=(select auth.uid()) and not hidden) with check(author_id=(select auth.uid()) and not hidden);
create policy posts_delete on public.posts for delete to authenticated using(author_id=(select auth.uid()));
create policy comments_read on public.comments for select using(not hidden and exists(select 1 from public.posts where id=post_id));
create policy comments_insert on public.comments for insert to authenticated with check(author_id=(select auth.uid()) and not hidden and exists(select 1 from public.posts where id=post_id));
create policy comments_update on public.comments for update to authenticated using(author_id=(select auth.uid()) and not hidden) with check(author_id=(select auth.uid()) and not hidden and exists(select 1 from public.posts where id=post_id));
create policy comments_delete on public.comments for delete to authenticated using(author_id=(select auth.uid()));
create policy reactions_read on public.reactions for select to authenticated using(user_id=(select auth.uid()) and exists(select 1 from public.posts where id=post_id));
create policy reactions_insert on public.reactions for insert to authenticated with check(user_id=(select auth.uid()) and exists(select 1 from public.posts where id=post_id));
create policy reactions_delete on public.reactions for delete to authenticated using(user_id=(select auth.uid()));
create policy providers_read on public.providers for select using(approved);
create policy advertisements_read on public.advertisements for select using(approved and now()>=starts_at and now()<ends_at and exists(select 1 from public.providers where id=provider_id));

-- Aggregate only, never expose the identities of other members who reacted.
create function public.reaction_count(target uuid) returns bigint
language sql stable security definer set search_path=''
as $$ select count(*) from public.reactions r join public.posts p on p.id=r.post_id where p.id=target and not p.hidden $$;
revoke all on function public.reaction_count(uuid) from public;
grant execute on function public.reaction_count(uuid) to anon,authenticated;
commit;
