# jpaBasic
JAP의 목적 : 데이터를 자바 컬렉션 다루 듯 하는 것

1. entityManagerFactory 에서 entityManager를 얻어온다.
2. entityManager를 활용하여 작업을 수행한다.   
3. EntityTransaction tx = entityManager.getTransaction();
   - jpa의 모든 작업은 transaction 내에서 수행 되어야 한다.
   - tx.begin();
   - entityManager.persist("객체"); : insert
   - entityManager.find(객체타입.class, key값); : select
   - 객체.setField(""); : update
   - entityManager.remove("객체"); : delete
   - tx.commit();

em.createQuery("select m from Member as m").setFirstResult(3).setMaxResult(5).getResultList();
 - entityManager를 이용하여 SQL문 활용도 가능
 - 객체 기반
 - 페이징처리 가능


# 영속성 컨택스트(persistence context)

