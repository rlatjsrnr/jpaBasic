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
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
