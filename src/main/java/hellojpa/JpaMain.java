package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        // em는 쓰레드 간 공유 하면 안된다. (사용후 버려야 한다.)
        // jpa는 모든 작업이 transaction 내에서 이루어 져야 한다.

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try {
            /*
            Member member = new Member();
            member.setId(1L);
            member.setName("hello");

            // insert
            em.persist(member);

            // select
            Member member1 = em.find(Member.class, 1L);

            // update
            member1.setName("helloow");

            // delete
            em.remove(member1);


            // Member findMember = em.find(Member.class, 2L);
            List<Member> result = em.createQuery("select m from Member AS m" , Member.class)
                    .setFirstResult(5)
                    .setMaxResults(7)
                    .getResultList();

            for(Member m : result){
                System.out.println("Member.name = " + m.getName());
            }

            Member findMember1 = em.find(Member.class, 2L);
            Member findMember2 = em.find(Member.class, 2L);

            System.out.println("result = " + (findMember1 == findMember2));


            // insert 문을 영속성 컨택스트 내에 저장 해두고 커밋이 되면 한 번에 모든 insert 를 수행

            Member member3 = new Member(150L, "A");
            Member member4 = new Member(160L, "B");

            em.persist(member3);
            em.persist(member4);


            Member member5 = new Member(170L, "C");
            em.persist(member5);
            member.setName("ZZZZZ");

            Member member6 = new Member(200L, "member6");
            em.persist(member6);
            em.flush();

            Member mem = em.find(Member.class, 200L);
            mem.setName("AAAAA");
            // 특정 entity만 준 영속 상태로 전환
            em.detach(mem);
            // 영속성 컨택스트 초기화
            em.clear();

            System.out.println("==========================");
            mem = em.find(Member.class, 200L);



            Member member = new Member();
            member.setId(2L);
            member.setUsername("user1");
            member.setRoleType(RoleType.ADMIN);
            em.persist(member);


            Member member1 = new Member();
            member1.setUsername("A");

            Member member2 = new Member();
            member2.setUsername("B");

            Member member3 = new Member();
            member3.setUsername("C");


            System.out.println("==================================");
            em.persist(member1); // 1 / 51
            em.persist(member2); // MEM
            em.persist(member3); // MEM

            System.out.println("member1 : " + member1.getId());
            System.out.println("member2 : " + member2.getId());
            System.out.println("member3 : " + member3.getId());
            System.out.println("==================================");
             */

            // 저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member_team member = new Member_team();
            member.setName("member1");
            //member.setTeamId(team.getId());
            member.setTeam(team); //**
            em.persist(member);

            // 이건 jpa가 안봄 주인이 아니면 읽기전용임 but 양방향 맵핑은 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정해야 한다.
            // 이거 실수할 수 있다. 연관관계 편의 매소드를 사용하자. member.changTeam(team);
            // team.getMembers().add(member); //**

            //em.flush();
            //em.clear();

            Team findTeam = em.find(Team.class, team.getId());
            List<Member_team> members = findTeam.getMembers();

            for (Member_team m : members) {
                System.out.println("m = " + m.getName());
            }

            /*
            Member_team findMember = em.find(Member_team.class, member.getId());
            // Long findTeamId = findMember.getTeamId();
            Team findTeam = findMember.getTeam();
            System.out.println("findTeam : " + findTeam.getName());

            // 연관관계 수정
            //Team newTeam = em.find(Team.class, 100L);
            //findMember.setTeam(newTeam);

            List<Member_team> members = findMember.getTeam().getMembers();

            for(Member_team m : members){
                System.out.println("m = " + m.getName());
            }*/


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();


    }
}
