package com.wootecam.festivals.domain.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberIdResponse;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("MemberService 테스트")
class MemberServiceTest extends SpringBootTestConfig {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        clear();
    }

    @Nested
    @DisplayName("회원 가입")
    class CreateMember {

        @Test
        @DisplayName("새로운 회원을 생성한다")
        void createNewMember() {
            // given
            MemberCreateRequest dto = new MemberCreateRequest("test", "test@test.com", "test");

            // when
            MemberIdResponse memberIdResponse = memberService.createMember(dto);
            Long memberId = memberIdResponse.id();
            // then
            Optional<Member> foundMember = memberRepository.findById(memberId);
            assertAll(
                    () -> assertNotNull(memberId),
                    () -> assertTrue(foundMember.isPresent()),
                    () -> assertEquals(dto.name(), foundMember.get().getName()),
                    () -> assertEquals(dto.email(), foundMember.get().getEmail()),
                    () -> assertEquals(dto.profileImg(), foundMember.get().getProfileImg())
            );
        }

        @Test
        @DisplayName("중복된 이메일로 가입 시 예외가 발생한다")
        void throwExceptionForDuplicateEmail() {
            // given
            MemberCreateRequest dto = new MemberCreateRequest("test", "test@test.com", "test");
            memberService.createMember(dto);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(dto))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.DUPLICATED_EMAIL);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class WithdrawMember {

        @Test
        @DisplayName("성공 시 회원은 삭제 상태로 존재해야 한다")
        void memberShouldBeMarkedAsDeletedAfterWithdrawal() {
            // given
            MemberIdResponse memberIdResponse = memberService.createMember(
                    new MemberCreateRequest("test", "test@test.com", "test"));
            Long memberId = memberIdResponse.id();
            // when
            memberService.withdrawMember(memberId);

            // then
            Optional<Member> member = memberRepository.findById(memberId);
            assertAll(
                    () -> assertTrue(member.isPresent()),
                    () -> assertTrue(member.get().isDeleted())
            );
        }

        @Test
        @DisplayName("존재하지 않는 회원 탈퇴 시 예외가 발생한다")
        void throwExceptionForNonExistentMember() {
            // given
            Long nonExistentMemberId = 1L;

            // when & then
            assertThatThrownBy(() -> memberService.withdrawMember(nonExistentMemberId))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class FindMember {
        @Test
        @DisplayName("회원 조회 성공 테스트")
        void findMember_Success() {
            // given
            MemberIdResponse memberIdResponse = memberService.createMember(
                    new MemberCreateRequest("test name", "test@example.com", "test-profile-img"));
            Long memberId = memberIdResponse.id();
            // when
            MemberResponse response = memberService.findMember(memberId);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(memberId, response.id()),
                    () -> assertEquals("test name", response.name()),
                    () -> assertEquals("test@example.com", response.email()),
                    () -> assertEquals("test-profile-img", response.profileImg())
            );
        }

        @Test
        @DisplayName("회원 조회 실패 테스트 - 존재하지 않는 회원")
        void findMember_UserNotFound() {
            // given
            Long nonExistentMemberId = 999L;

            // when & then
            assertThatThrownBy(() -> memberService.findMember(nonExistentMemberId))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.USER_NOT_FOUND);
        }
    }
}