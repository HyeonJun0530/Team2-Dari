package com.wootecam.festivals.domain.ticket.entity;

import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    @DisplayName("티켓 생성에 성공한다.")
    void createTicket() {
        LocalDateTime now = LocalDateTime.now();
        Festival festival = FestivalStub.createFestivalWithTime(now, LocalDateTime.now().plusDays(7));
        Ticket ticket = Ticket.builder()
                .festival(festival)
                .name("티켓 이름")
                .detail("티켓 상세")
                .price(10000L)
                .quantity(100)
                .startSaleTime(now)
                .endSaleTime(now.plusDays(1))
                .refundEndTime(now.plusDays(1))
                .build();

        assertAll(
                () -> assertEquals(festival, ticket.getFestival()),
                () -> assertEquals("티켓 이름", ticket.getName()),
                () -> assertEquals("티켓 상세", ticket.getDetail()),
                () -> assertEquals(10000L, ticket.getPrice()),
                () -> assertEquals(100, ticket.getQuantity()),
                () -> assertEquals(now, ticket.getStartSaleTime()),
                () -> assertEquals(now.plusDays(1), ticket.getEndSaleTime()),
                () -> assertEquals(now.plusDays(1), ticket.getRefundEndTime())
        );
    }

    @Nested
    @DisplayName("티켓이 생성되었다면")
    class Describe_createTicketStock {
        LocalDateTime now = LocalDateTime.now();
        Festival festival = FestivalStub.createFestivalWithTime(now,
                LocalDateTime.now().plusDays(7));
        Ticket ticket = Ticket.builder()
                .festival(festival)
                .name("티켓 이름")
                .detail("티켓 상세")
                .price(10000L)
                .quantity(100)
                .startSaleTime(now)
                .endSaleTime(now.plusDays(1))
                .refundEndTime(now.plusDays(1))
                .build();

        @Test
        @DisplayName("티켓 재고 생성에 성공한다.")
        void it_returns_ticketStock() {
            List<TicketStock> ticketStocks = ticket.createTicketStock();

            assertAll(
                    () -> assertThat(ticketStocks.size()).isEqualTo(ticket.getQuantity()),
                    () -> assertThat(ticketStocks.get(0).getTicket()).isEqualTo(ticket)
            );
        }

        @Test
        @DisplayName("티켓 구매 내역 생성에 성공한다.")
        void it_returns_purchase() {
            Member member = createMember("testUser", "testEmail@test.com");
            Purchase purchase = ticket.createPurchase(member);

            assertAll(
                    () -> assertThat(purchase).isNotNull(),
                    () -> assertThat(purchase.getTicket()).isEqualTo(ticket),
                    () -> assertThat(purchase.getMember()).isEqualTo(member),
                    () -> assertThat(purchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASED),
                    () -> assertThat(purchase.getPurchaseTime()).isBeforeOrEqualTo(LocalDateTime.now())
            );
        }
    }
}
