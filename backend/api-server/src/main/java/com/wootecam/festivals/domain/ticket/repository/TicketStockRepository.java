package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {

    @Query(value = """
            SELECT * FROM ticket_stock ts  WHERE ts.ticket_id = :#{#ticketId} AND ts.ticket_stock_member_id IS NULL
             LIMIT 1 FOR UPDATE SKIP LOCKED;
            """, nativeQuery = true)
    Optional<TicketStock> findByTicketForUpdate(@Param("ticketId") Long ticketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TicketStock ts WHERE ts.id = :id")
    Optional<TicketStock> findByIdForUpdate(Long id);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM TicketStock ts WHERE ts.id = :id AND ts.memberId = :memberId AND ts.ticket.id = :ticketId) THEN true ELSE false END")
    boolean existsByIdAndTicketIdAndMemberId(@Param("id") Long id, @Param("ticketId") Long ticketId,
                                             @Param("memberId") Long memberId);

    @Query("SELECT ts FROM TicketStock ts WHERE ts.id = :id AND ts.memberId = :memberId AND ts.ticket.id = :ticketId")
    Optional<TicketStock> findByIdAndTicketIdAndMemberId(@Param("id") Long id, @Param("ticketId") Long ticketId,
                                                         @Param("memberId") Long memberId);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM TicketStock ts WHERE ts.ticket = :ticket AND ts.memberId = :memberId) THEN true ELSE false END")
    boolean existsByTicketAndMember(Ticket ticket, Long memberId);
}
