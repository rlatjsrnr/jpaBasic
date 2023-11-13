# JPA
- [JPA](#jpa)  
  * [jpaBasic](#jpabasic)
  * [영속성 컨택스트(persistence context)](#영속성-컨택스트(persistence-context))
  * [flush()](#flush())

## jpaBasic

JAP의 목적 : 데이터를 자바 컬렉션 다루 듯 하는 것

1. entityManagerFactory 에서 entityManager를 얻어온다.
2. EntityTransaction tx = entityManager.getTransaction();
   - jpa의 모든 작업은 transaction 내에서 수행 되어야 한다.
   - tx.begin();
   - tx.commit(); 
3. entityManager를 활용하여 작업을 수행한다.
   - entityManager.persist("객체"); : insert
   - entityManager.find(객체타입.class, key값); : select
   - 객체.setField(""); : update
   - entityManager.remove("객체"); : delete   

entityManager.createQuery("select m from Member as m").setFirstResult(3).setMaxResult(5).getResultList();
 - entityManager를 이용하여 SQL문 활용도 가능
 - 객체 기반
 - 페이징처리 가능

## 영속성 컨택스트(persistence context)

- Entity를 영구 저장하는 환경
- EntityManager를 통해 영속성 컨택스트에 접근
- EntityManager와 영속성 컨택스트는 1:1로 매칭된다.
- Entity의 생명주기
  - 비영속(new / transient) : 영속성 컨텍스트와 전혀 관계가 없는새로운 상태
  - 영속(managed) : 영속성 컨택스트에 관리되는 상태
  - 준영속(detached) : 영속성 컨텍스트에 저장되었다가 분리된 상태
  - 삭제(remove) : 삭제된 상태
   <img width="530" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/eb64d4f5-5af3-4b43-a7cb-063e29a59c42">

- 영속성 컨텍스트의 이점
  - 1차 캐시
  - 동일성(identity) 보장
  - 트랜잭션을 지원하는 쓰기 지연 (transactional write-behind)
  - 변경 감지(Dirty Checking)
  - 지연 로딩(Lazy Loading)

+ JPA는 em.persist(객체), 즉 insert를 한 경우 바로 insert 쿼리를 날리는게 아니라 entity를 1차 캐시에 저장하고 쓰기 지연 SQL 저장소에 insert 쿼리문을 저장해 둔다. 
+ 또한 조회를 하면 먼저 1차 캐시 안에서 찾고, 만약 없다면 DB에서 entitiy를 가져와 1차 캐시에 저장한 후 반환한다.
+ 같은 트랜젝션 내에서 같은 entity를 조회를 할 경우는 1차 캐시 내의 같은 entity를 조회 하기 때문에 동일성이 보장된다.
   <img width="320" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/70ef3878-ad4a-4395-a63a-c56998157e8c">

   ```
   Member a = em.find(Member.class, "member1");
   Member b = em.find(Member.class, "member1");
   System.out.println(a == b); // true
   ```
+ 1차 캐시 내에는 저장되는 entitiy마다 스냅샷이 함께 저장이 된다.
+ 위의 Member타입의 a를 수정 하기 위해 a.setName("AAA"); 를 실행하면 update 쿼리문에 쓰기 지연 SQL 저장소에 저장된다.
+ commit전에 flush()를 통해 쓰기 지연 SQL 저장소에 저장된 쿼리들을 DB로 보내 영속성 컨택스트의 변경내용을 데이터베이스에 반영한다.
+ em.remove(a) 삭제시에도 위와 같은 과정을 거친다.

## flush()

+ flush() : 영속성 컨텍스트의 변경내용을 데이터베이스에 반영
+ flush() 호출 방법
  - entityManager.flush() 를 통한 직접 호출
  - 트랜젝션의 commit 실행시 flush() 자동 호출
  - JPQL 쿼리 실행시 flush() 자동 호출
+ flush()는 영속성 컨택스트를 비우지 않는다.
+ 그냥 영속성 컨택스트와 DB를 동기화 하는 작업
+ 트랜잭션이라는 작업 단위에서 모든 동작이 이루어지므로 commit전에만 동기화를 하면 된다.
+ JPQL 쿼리 실행시 flush()가 자동 호출되는 이유
```
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```
 - 만약 JPQL전 flush()가 동작하지 않는다면 위와 같은 상황에서 memberA, memberB, memberC는 영속성 컨택스트에만 반영된 상태이고 DB에는 없기 존재하지 않기 때문에 쿼리문 실행에 문제가 발생 할 수 있다.
 - 이러한 문제를 방지하기 위하여 JPQL 쿼리 실행시 먼저 flush()를 호출하여 영속성 컨택스트와 DB의 동기화 작업을 수행한다.
     
