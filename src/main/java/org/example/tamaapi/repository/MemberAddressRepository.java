package org.example.tamaapi.repository;

import org.example.tamaapi.domain.Member;
import org.example.tamaapi.domain.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
    //Optional<MemberAddress> findByMemberIdAndZipCodeAndStreetAndDetail(Long memberId, String zipCode, String Street, String detail);

    boolean existsByMemberIdAndZipCodeAndStreetAndDetail(Long memberId, String zipCode, String Street, String detail);


    boolean existsByMemberId(Long memberId);

    List<MemberAddress> findAllByMemberId(Long memberId);

    Optional<MemberAddress> findByMemberIdAndIsDefault(Long memberId, Boolean isDefault);
}
