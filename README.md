# JPA
- [JPA](#jpa)  
  * [jpaBasic](#jpabasic)
  * [영속성 컨택스트(persistence context)](#영속성-컨택스트persistence-context)
  * [flush()](#flush)
  * [Entity 매핑](#Entity-매핑)
  * [연관관계 매핑 기초](#연관관계-매핑-기초)

## jpaBasic

JPA의 목적 : 데이터를 자바 컬렉션 다루 듯 하는 것

JPA의 기본
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

   ```java
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
```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```
 - 만약 JPQL전 flush()가 동작하지 않는다면 위와 같은 상황에서 memberA, memberB, memberC는 영속성 컨택스트에만 반영된 상태이고 DB에는 없기 존재하지 않기 때문에 쿼리문 실행에 문제가 발생 할 수 있다.
 - 이러한 문제를 방지하기 위하여 JPQL 쿼리 실행시 먼저 flush()를 호출하여 영속성 컨택스트와 DB의 동기화 작업을 수행한다.

## Entity 매핑
 - @Entity : JPA가 관리하는 클래스 엔티티, 테이블과 매핑할 클래스
   - name : JPA에서 사용할 엔티티 이름 지정하는 속성, 기본값은 클래스 이름
 - @Table : 엔티티와 매핑할 테이블을 지정한다.

 - 데이터베이스 스키마 자동 생성
   - DDL을 어플리케이션 실행 시점에 자동 생성
   - 데이터베이스 방언을 적절히 활용
   - 개발 시에만 쓰자
      
   - hibernate.hbm2ddl.auto
   - create : 기존 테이블 삭제 후 다시 생성(DROP + CREATE)
   - create-drop : create와 같으나 종료 시점에 drop
   - update : 변경분만 반영
   - validate : 엔티티와 테이블이 정상 매핑되었는지만 확인
   - none : 사용하지 않음
     
- 제약조건 추가
  - @Column(nullable = false, length = 10) : not null, 10글자 제한
  - @Table(uniqueConstraints = {@UniqueConstraint( name = "제약조건 이름", columnNames = {"컬럼 이름"} )})
  - DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.

- 필드와 컬럼 매핑
  - @Column : 컬럼 매핑
    - name : 필트와 매핑할 테이블의 컬럼 이름, 기본값 객체의 필드 이름
    - insertable, updatable : 등록, 변경 가능 여부, 기본값 TRUE
    - nullable(DDL) : null값 허용 여부, false 설정 시 DDL 생성 시에 not null 제약조건 추가한다.
    - unique(DDL) : unique 제약 조건을 건다
    - columnDefinition(DDL) : 데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default 'EMPTY' - 입력한 값이 쿼리로 그대로 들어감
    - length(DDL) : 문자 길이 제약조건, String 타입에만 사용
    - precision, scale(DDL) : BigDecimal 타입에서 사용한다. precision은 소수점을 포함한 전체 자릿수, scale은 소수 자릿수.
  - @Temporal : 날짜 타입 매핑
    - TemporalType.DATE : 날짜, DB의 date 타입과 메핑
    - TemporalType.TIME : 시간, DB의 time 타입과 매핑
    - TemporalType.TIMESTAMP : 날짜와 시간, DB의 timestamp 타입과 매핑
  - @Enumerated : enum 타입 매핑
    - EnumType.ORDINAL : enum 순서를 데이터베이스에 저장
    - EnumType.STRING : enum 이름을 데이터베이스에 저장
    - EnumType.String을 쓰자!!
  - @Lob : BLOB, CLOB 매핑
    - 문자면 CLOB, 나머지는 BLOB 매핑
  - @Transient : 특정 필드를 컬럼에 매핑하지 않음(매핑 무시)
    - 필드를 매핑하지 않음
    - DB에 저장 또는 조회 하지 않음
    - 메모리상에 임시로 어떤 값을 보관하고 싶을 때 사용
   
  - 기본 키 매핑 어노테이션
    - @Id : 이 필드가 기본키 필드다.
    - @GeneratedValue : 키 값 자동 생성
      - IDENTITY : 데이터베이스에 위임, MySQL의 auto_increment
        - 데이터베이스의 키 생성은 INSERT SQL을 실행 해야 됨
        - JPA는 보통 transaction commit 시점에 INSERT SQL을 실행함
        - IDENTITY 전략을 사용 할 경우 em.persist() 시점에 INSERT SQL을 실행하고 DB에서 식별자를 조회 한다.
      - SEQUNCE : 데이터베이스의 시퀀스 오브젝트 사용, oracle 
        - 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 데이터베이스 오브젝트이다.
        - @SequnceGenerator() 어노테이션이 필요함
          - name : SequenceGenerator 의 이름, 필수 값
          - sequnceName : 데이터베이스에 등록되어 있는 시퀀스 오브젝트 이름
          - initialValue : DDL 생성 시에 사용, 처음 시작하는 값 지정, 기본 값은 1
          - allocationSize : 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용) 기본값 50
            - 1~50 까지를 한번에 가져오고 메모리에 저장 해 둔 후 순서대로 사용 DB접근 수를 줄임              
      - TABLE : 키 생성용 테이블 사용
        - 키 생성 전용 테이블을 하나 만들어 데이터베이스의 시퀀스를 흉내낸다.
        - 장점 : 모든 데이터베이스에 적용 가능
        - 단점 : 성능
      - AUTO : 방언에 따라 자동 지정, 기본값

      - 권장 하는 식별자 전략
        - 기본 키 제약 조건 : null 아님, 유일, 변하면 안됨.
        - 이러한 자연키를 찾기는 어렵다. 대리키(대체키)를 사용하자.
        - Long타입 + 대체키 + 키 생성 전략 사용
        
    
## 연관관계 매핑 기초
- 연관관계가 필요한 이유
  - 테이블은 외래 키로 조인을 사용하여 연관된 테이블을 찾는다.
  - 객체는 참조를 사용해서 연관된 객체를 찾는다.
  - 테이블과 객체는 다르다.
- 단방향 연관관계

<img width="599" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/9679f26d-8978-4c4b-b8bf-b27f94d7929b">

  - 연관관계 저장
    
```java
//팀 저장
 Team team = new Team();
 team.setName("TeamA");
 em.persist(team);
 //회원 저장
 Member member = new Member();
 member.setName("member1");
 member.setTeam(team); //단방향 연관관계 설정, 참조 저장
 em.persist(member);
```

 - 연관관계 조회 - 객체 그래프 탐
```java
//조회
 Member findMember = em.find(Member.class, member.getId());
//참조를 사용해서 연관관계 조회
 Team findTeam = findMember.getTeam();

```

 - 연관관계 수정
```java
// 새로운 팀B
 Team teamB = new Team();
 teamB.setName("TeamB");
 em.persist(teamB);
 // 회원1에 새로운 팀B 설정
 member.setTeam(teamB);
```

- 양방향 연관관계와 연관관계의 주인
  
<img width="643" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/3f24eb89-b440-4318-91cc-bb46c7d300dc">

 - 외래키를 가지는 쪽이 주인이 된다.
 - 외래키는 다(多) 쪽이 가진다.
 - @ManyToOne : 다(多) 쪽 테이블의 외래키가 가르키는 객체 필드
 - @JoinColumn(name = "외래키 필드") : 외래키
 - @OneToMany(maapedBy = "team") : 다 대 1에서 1 쪽의 필드
   - mappedBy : 이 필드는 mappedBy로 지정한 필드의 거울이다 조회 기능을 한다.
 - 주의 
   - 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정해 주자.
   - 연관관계 편의 매소드를 생성하자.

```java
public void addMember(Member member){
  members.add(member);
  member.setTeam(this);
}
```
 - 양방향 매핑시에 무한 루프를 조심하자.
   - toString() : member -> team -> members -> member들 -> team들 -> members들 .....
   - lombok
   - JSON 생성 라이브러리


