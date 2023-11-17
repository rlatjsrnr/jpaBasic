package hellojpa;

import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
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
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);

            em.flush();
            em.clear();

            Parent findParent = em.find(Parent.class, parent.getId());
            em.remove(findParent);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e.getMessage());
        } finally {
            em.close();
        }
        emf.close();

    }

    private static void printMember(Member_team findMember) {
        System.out.println("member = " + findMember.getName());
    }

    private static void printMemberAndTeam(Member_team findMember) {
        String username = findMember.getName();
        System.out.println("username = " + username);

        Team team = findMember.getTeam();
        System.out.println("team = " + team.getName());
    }

    private static void logic(Member m1, Member m2){
        System.out.println("m1 == m2 " + (m1 instanceof Member));
        System.out.println("m1 == m2 " + (m2 instanceof Member));
    }

}
