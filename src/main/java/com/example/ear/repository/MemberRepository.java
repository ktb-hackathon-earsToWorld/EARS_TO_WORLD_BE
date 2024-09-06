package com.example.ear.repository;

import com.example.ear.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member , Long> {

    Member findByLoginId(String loginId);
}
