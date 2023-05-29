package oneny.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class MemberTest {

  @Autowired
  MemberService memberService;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  LogRepository logRepository;

  /**
   * MemberService @Transactional: ON
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional(REQUIRES_NEW) Exception
   */
  @Test
  void recoverException_success() {
    // given
    String username = "로그예외_outerTx_success";

    // when
    memberService.joinV2(username);

    // then: 모든 데이터가 롤백된다.
    assertThat(memberRepository.find(username)).isPresent();
    assertThat(logRepository.find(username)).isEmpty();
  }

  /**
   * MemberService @Transactional: ON
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional: ON Exception
   */
  @Test
  void recoverException_fail() {
    // given
    String username = "로그예외_outerTx_success";

    // when
    assertThatThrownBy(() -> memberService.joinV2(username))
            .isInstanceOf(UnexpectedRollbackException.class); // Participating transaction failed - marking existing transaction as rollback-only

    // then: 모든 데이터가 롤백된다.
    assertThat(memberRepository.find(username)).isEmpty();
    assertThat(logRepository.find(username)).isEmpty();
  }

  /**
   * MemberService @Transactional: ON
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional: ON
   */
  @Test
  void outerTxOn_success() {
    // given
    String username = "outerTx_success";

    // when
    memberService.joinV1(username);

    // then: 모든 데이터가 정상 저장된다.
    assertThat(memberRepository.find(username)).isPresent();
    assertThat(logRepository.find(username)).isPresent();
  }

  /**
   * MemberService @Transactional: ON
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional: ON Exception
   */
  @Test
  void outerTxOn_fail() {
    // given
    String username = "로그예외_outerTx_success";

    // when
    assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);

    // then: 모든 데이터가 롤백된다.
    assertThat(memberRepository.find(username)).isEmpty();
    assertThat(logRepository.find(username)).isEmpty();
  }

  /**
   * MemberService @Transactional: ON
   * MemberRepository @Transactional: OFF
   * LogRepository @Transactional: OFF
   */
  @Test
  void singleTx() {
    // given
    String username = "singleTx";

    // when
    memberService.joinV1(username);

    // then: 모든 데이터가 저장된다.
    assertThat(memberRepository.find(username)).isPresent();
    assertThat(logRepository.find(username)).isPresent();
  }

  /**
   * MemberService @Transactional: OFF
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional: ON
   */
  @Test
  void outerTxOff_success() {
    // given
    String username = "outerTxOff_success";

    // when
    memberService.joinV1(username);

    // then: 모든 데이터가 정상 저장된다.
    assertThat(memberRepository.find(username)).isPresent();
    assertThat(logRepository.find(username)).isPresent();
  }


  /**
   * MemberService @Transactional: OFF
   * MemberRepository @Transactional: ON
   * LogRepository @Transactional: ON Exception
   */
  @Test
  void outerTxOff_fail() {
    // given
    String username = "로그예외outerTxOff_success";

    // when
    assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);

    // then: 완전히 롤백되지 않고, member 데이터가 남아서 저장된다.
    assertThat(memberRepository.find(username)).isPresent();
    assertThat(logRepository.find(username)).isEmpty();
  }
}
