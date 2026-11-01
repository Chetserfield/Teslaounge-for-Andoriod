package kr.teslounge.app;

import android.content.Context;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class AppFlowTest {
    @Before public void resetDemoData() {
        InstrumentationRegistry.getInstrumentation().getTargetContext()
            .getSharedPreferences("teslounge_v1", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test public void ownerProfileChangesNavigationAndSurvivesRelaunch() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("관심 설정하기")).perform(scrollTo(), click());
            onView(withSpinnerText("관심자")).perform(scrollTo(), click());
            onData(equalTo("오너")).perform(click());
            onView(withHint("닉네임 (20자)")).perform(scrollTo(), replaceText("테스트 오너"), closeSoftKeyboard());
            onView(withText("프로필 저장")).perform(scrollTo(), click());
            onView(withText("오너의 라운지")).check(matches(isDisplayed()));
            onView(withText("내 차")).perform(click());
            onView(withText("연결된 차량이 없어요")).check(matches(isDisplayed()));
            app.recreate();
            onView(withText("연결된 차량이 없어요")).check(matches(isDisplayed()));
        }
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("오너의 라운지")).check(matches(isDisplayed()));
            onView(allOf(withText("MY"), isAssignableFrom(Button.class))).perform(click());
            onView(withHint("닉네임 (20자)")).perform(scrollTo()).check(matches(withText("테스트 오너")));
            onView(withSpinnerText("오너")).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test public void writeLikeCommentAndDeletePost() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("커뮤니티")).perform(click());
            onView(withText("＋ 이야기 쓰기")).perform(click());
            onView(withText("기기에만 저장하며 체험하기")).perform(scrollTo(), click());
            onView(withText("기기에 저장")).perform(click());
            onView(withHint("제목 (80자 이내)")).check(matches(hasErrorText("제목을 입력해주세요")));
            onView(withHint("제목 (80자 이내)")).perform(replaceText("테스트 드라이브"));
            onView(withHint("함께 나누고 싶은 이야기 (3,000자)")).perform(replaceText("첫 드라이브를 준비합니다."), closeSoftKeyboard());
            onView(withText("기기에 저장")).perform(click());
            onView(withText("♡ 공감하기")).perform(scrollTo(), click());
            onView(withText("♥ 공감 취소")).check(matches(isDisplayed()));
            onView(withHint("따뜻한 댓글을 남겨주세요")).perform(scrollTo(), replaceText("좋은 여행 되세요"), closeSoftKeyboard());
            onView(withText("댓글 저장")).perform(scrollTo(), click());
            app.recreate();
            onView(withText("좋은 여행 되세요")).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText("내 글 삭제")).perform(scrollTo(), click());
            onView(withText("삭제")).perform(click());
            onView(withHint("제목이나 내용 검색")).perform(replaceText("테스트 드라이브"), closeSoftKeyboard());
            onView(withText("아직 이야기가 없어요. 첫 글을 남겨보세요.")).check(matches(isDisplayed()));
        }
    }

    @Test public void guestCannotBecomeMemberThroughUnconfiguredKakao() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("커뮤니티")).perform(click());
            onView(withText("＋ 이야기 쓰기")).perform(click());
            onView(withText("카카오로 시작하기")).perform(scrollTo(), click());
            onView(withText("아직 카카오 계정에 연결되지 않았어요")).check(matches(isDisplayed()));
            onView(withText("확인")).perform(click());
            onView(withText("먼저 둘러보기")).perform(scrollTo(), click());
            onView(withText("함께 나누는 이야기")).check(matches(isDisplayed()));
            onView(withText("＋ 이야기 쓰기")).perform(click());
            onView(withText("카카오로 시작하기")).check(matches(isDisplayed()));
        }
    }

    @Test public void cancelLoginKeepsCommentAndEndingPreviewRestoresGate() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("커뮤니티")).perform(click());
            onView(withText("우리 동네 오너들과 커피 한 잔")).perform(scrollTo(), click());
            onView(withHint("따뜻한 댓글을 남겨주세요")).perform(scrollTo(), replaceText("보존할 댓글"), closeSoftKeyboard());
            onView(withText("댓글 저장")).perform(scrollTo(), click());
            onView(withText("먼저 둘러보기")).perform(scrollTo(), click());
            onView(withHint("따뜻한 댓글을 남겨주세요")).check(matches(withText("보존할 댓글")));
            onView(withText("댓글 저장")).perform(scrollTo(), click());
            onView(withText("기기에만 저장하며 체험하기")).perform(scrollTo(), click());
            onView(withText("보존할 댓글")).perform(scrollTo()).check(matches(isDisplayed()));
            app.onActivity(activity -> activity.go("MY"));
            onView(withText("기기 체험 끝내기")).perform(scrollTo(), click());
            onView(withText("게스트로 둘러보는 중")).check(matches(isDisplayed()));
            onView(withText("커뮤니티")).perform(click());
            onView(withText("＋ 이야기 쓰기")).perform(click());
            onView(withText("카카오로 시작하기")).check(matches(isDisplayed()));
        }
    }

    @Test public void everyAudienceCanFindVendorsAndResetFilters() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            for(int group=0;group<3;group++){
                final int value=group;
                app.onActivity(activity->{activity.prefs.edit().putInt("group",value).commit();activity.go("홈");});
                onView(withText("케어")).perform(click());
                onView(withText("내 차를 위한 케어")).check(matches(isDisplayed()));
            }
            onView(withHint("업체명 또는 지역 검색")).perform(replaceText("서울"),closeSoftKeyboard());
            onView(withSpinnerText("전체 서비스")).perform(click());
            onData(equalTo("PPF")).perform(click());
            onView(withText(containsString("PPF · “서울”"))).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText("검색 조건 초기화")).perform(scrollTo(),click());
            onView(withHint("업체명 또는 지역 검색")).perform(scrollTo()).check(matches(withText("")));
            onView(withSpinnerText("전체 서비스")).check(matches(isDisplayed()));
        }
    }

    @Test public void checklistSurvivesRelaunch() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("내 체크리스트 열기")).perform(scrollTo(), click());
            onView(withText("공식 안내에서 인도 일정과 장소 확인")).perform(click());
            onView(withText("1 / 7 완료")).check(matches(isDisplayed()));
        }
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("내 체크리스트 열기")).perform(scrollTo(), click());
            onView(withText("공식 안내에서 인도 일정과 장소 확인")).check(matches(isChecked()));
            onView(withText("1 / 7 완료")).check(matches(isDisplayed()));
        }
    }

    @Test public void stockChartHasProviderNoticeAndReloadControl() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("뉴스·주식")).perform(click());
            onView(withText("새로운 소식을 기다려요")).check(matches(isDisplayed()));
            onView(withText("TSLA 주식 보기")).perform(click());
            onView(withText("TradingView · TSLA")).check(matches(isDisplayed()));
            onView(withText("차트 다시 불러오기")).perform(scrollTo(), click());
            onView(withText("TradingView · TSLA")).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test public void prospectiveOwnerCanReachDeliveryAndServices() {
        try (ActivityScenario<MainActivity> app = ActivityScenario.launch(MainActivity.class)) {
            onView(withText("관심 설정하기")).perform(scrollTo(), click());
            onView(withSpinnerText("관심자")).perform(scrollTo(), click());
            onData(equalTo("예비 오너")).perform(click());
            onView(withText("프로필 저장")).perform(scrollTo(), click());
            onView(withText("예비 오너의 라운지")).check(matches(isDisplayed()));
            onView(withText("케어")).perform(click());
            onView(withText("아직 등록된 업체가 없어요")).check(matches(isDisplayed()));
            onView(withText("커뮤니티")).perform(click());
            onView(withText("출고 센터 · 인도 체크리스트")).perform(click());
            onView(withText("나만의 인도 체크리스트")).check(matches(isDisplayed()));
        }
    }
}
