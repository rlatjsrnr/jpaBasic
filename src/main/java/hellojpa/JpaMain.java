package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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

             */
            Member member = new Member();
            member.setUsername("C");

            em.persist(member);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();


    }
}
