# JPA
- [JPA](#jpa)  
  * [jpaBasic](#jpabasic)
  * [영속성 컨택스트(persistence context)](#영속성-컨택스트persistence-context)
  * [flush()](#flush)
  * [Entity 매핑](#Entity-매핑)
  * [연관관계 매핑 기초](#연관관계-매핑-기초)
  * [다양한 연관관계 매핑](#다양한-연관관계-매핑)
  * [프록시와 연관관게 관리](#프록시와-연관관게-관리)

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

## 다양한 연관관계 매핑

- 연관관계 매핑시 고려사항 3가지
  - 다중성
    - 다대일 : @ManyToOne
    - 일대다 : @OneToMany
    - 일대일 : @OneToOne
    - 다대다 : @ManyToMany
  - 단방향, 양방향
    - 테이블 : 외래 키 하나로 양쪽 조인 가능
    - 객체 : 참조용 필드가 있는 쪽으로만 참조 가능, 한 쪽만 참조하면 단방향, 양쪽이 서로 참조하면 양방향      
  - 연관관계의 주인
    - 양방향 참조일 때, 둘 중에 외래키를 관리할 곳을 지정해야한다.
    - 외래키를 관리하는 쪽이 연관관계의 주인.
    - 주인의 반대편은 외래키에 영향을 주지 않는다. 단순 조회만 가능
- 다대일 단방향

   <img width="620" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/624d5497-a005-47d4-8eaf-dc3b3071ce13">

  - 가장 많이 사용하는 연관관계
  - 다대일의 반대는 일대다 이다.

- 다대일 양방향

  <img width="617" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/7598ee11-b124-4d10-a7fb-525ba78d1a62">

  - 외래키가 있는 쪽이 연관관계의 주인이다.
  - 외래키는 다 쪽에 있다.
  - 양쪽이 서로를 참조하도록 개발한다.
    
- 일대다 단방향

  <img width="587" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/d4f7cc21-d91f-41a2-8247-8fc61f69d041">

  - 일대다 단방향은 일대다에서 일이 연관관계의 주인
  - 테이블의 일대다 관계는 항상 다 쪽에 외래키가 있음
  - 객체와 테이블의 차이 때문에 반대편 테이블이 외래키를 관리하는 특이한 구조
  - @JoinColumn을 꼭 사용해야 함.
  - 단점
    - 엔티티가 관리하는 외래키가 다른 테이블에 있음
    - 연관관계 관리를 위하여 추가로 UPDATE SQL 실행 함
  - 일대다 단방향 매핑보단 다대일 양방향을 사용하자.

- 일대다 양방향

  <img width="554" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/d891fa3b-1706-41e8-a96e-d6c41f1f5071">

  - 공식적으론 없음
  - @JoinColumn(insertable=false, updatable=false)
  - 읽기 전용 필드를 사용해서 양방향 처럼 사용하는 방법
  - 다대일 양방향 쓰자.
 
- 일대일
   - 주 테이블이나 대상 테이블 중 외래키 선택 가능
   - 외래키에 데이터베이스 unique 제약조건 추가
- 일대일 주 테이블에 외래키 단방향

  <img width="513" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/01075433-e7e2-475b-a68b-997b9b63c31c">

   - 다대일(@ManyToOne) 단방향 매핑과 유사함.

- 일대일 주 테이블에 외래키 양방향

  <img width="543" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/e9aa7dff-f486-4503-8096-303652f0e202">

   - 다대일 양방향 매핑처럼 외래키가 있는 곳이 연관관계의 주인
   - 반대편은 mappedBy 적용

- 일대일 대상 테이블에 외래키 단방향은 지원X
- 일대일 대상 테이블에 외래키 양방향

  <img width="576" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/99773184-4dc4-42fe-b540-e5493f0548ef">

  - 일대일 주 테이블에 외래키 양방향과 매핑 방법은 같음
- 일대일 정리
  - 주 테이블에 외래 키
    - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래키를 두고 대상 테이블을 찾음
    - 객체지향 개발자가 선호하는 방식
    - JPA 매핑이 편리하다.
    - 장점 : 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
    - 단점 : 값이 없다면 외래 키에 null을 허용 해야함.
  - 대상 테이블에 외래 키
    - 대상 테이블에 외래키가 존재
    - DBA가 선호하는 방식
    - 장점 : 주 테이블과 대상 테이블을 일대일에서 일대다로 변경할 때 테이블 구조가 유지된다.
    - 단점 : 프록시 기능의 한계로 지연로딩 설정을 해도 즉시로딩 됨.
- 다대다
  - 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
  - 연결 테이블을 추가하여 일대다, 다대일로 풀어내야 함
  <img width="534" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/b05a663b-aa48-434a-9212-73ddbd2594b1">

  - @ManyToMany 사용하고 @JoinTable로 연결 테이블을 지정한다.
  - 다대다의 한계 : 실무에선 사용X, 연결테이블에 다른 데이터가 들어가야 함
  - 한계 극복 : 연결 테이블을 엔티티로 승격, @ManyToMany -> @OneToMany, @ManyToOne
  <img width="557" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/9fe8702c-8ef2-4edf-9746-7c90b1e2612c">

## 고급 매핑

- 상속 관계 매핑
  - 관계형 데이터베이스는 상속 관계가 없음
  - 슈퍼타입 서브타입 관계라는 모델링 기법에 객체 상속과 유사
  - 객체의 상속 구조와 DB의 슈퍼타임 서브타입 관계를 매핑

    <img width="594" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/fb2aba70-fd11-480e-852b-67f02b3b70c1">

  - 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
    - 각각 테이블로 변환 -> 조인 전략
    - 통합 테이블로 변환 -> 단일 테이블 전략
    - 서브타입 테이블로 변환 -> 구현 클래스마다 테이블 전략
    - 주요 어노테이션
      - @Inheritance(strategy = InheritanceType.XXX)
        - JOINED : 조인 전략
        - SINGLE_TABLE : 단일 테이블 전략
        - TABLE_PER_CLASS : 구현 클래스마다 테이블 전략
      - @DiscriminatorColumn(name = "DTYPE")
      - @DiscriminatorValue("XXX")

    - 조인 전략  
  <img width="645" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/9f17f643-d82e-4584-932a-814bc850c2ca">

      - 장점
        - 테이블 정규화
        - 외래 키 참조 무결성 제약조건 활용 가능
        - 저장공간 효율화
      - 단점
        - 조회시 조인을 많이 사용, 성능 저하
        - 조회 쿼리가 복잡
        - 데이터 저장시 INSERT SQL 두 번 호출
          
    - 단일 테이블 전략  
  <img width="545" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/22620804-fbd1-4b80-aa09-b5cd9da3ffa3">

      - 장점
        - 조인이 필요 없으므로 일반적으로 조회 성능 좋음
        - 조회 쿼리 단순함
      - 단점
        - 자식 엔티티가 매핑한 컬럼은 모두 null허용
        - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있음.
        - 상황에 따라선 오히려 조회 성능이 느려질 수도 있다.
         
     - 구현 클래스 마다 테이블 전략  
  <img width="645" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/eff6187e-870d-4edc-830b-f447f4a0d045">

       - 이 전략은 별로임
       - 장점
         - 서브 타입을 명확하기 구분해서 처리할 때 효과적
         - not null 사용가능
       - 단점
         - 여러 자식 테이블을 함께 조회할 때 성능 느림(UNION SQL 필요)
         - 자식 테이블을 통합해서 쿼리하기 힘듬

- @MappedSuperclass
  - 상속관계 매핑 X
  - 엔티티 X, 테이블과 매핑 X
  - 부모 클래스를 상속받는 자식 클래스에 매핑 정보만 제공
  - 조회, 검색 불가
  - 직접 생성해서 쓰지 않으므로 추상 클래스 권장
  - 여러 엔티티에서 공통으로 사용하는 정보를 모을 떄 사용한다.

## 프록시와 연관관게 관리
- 프록시
  - em.find() vs em.getReference()
  - em.find() : 데이터베이스를 통해 실제 엔티티 객체 조회
  - em.getReference() : 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회
  <img width="401" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/6b0e2769-24ad-4050-bb56-a05118e634d3">

  - 특징
    - 실제 클래스를 상속 받아 만들어 짐
    - 실제 클래스와 겉 모양이 같다
    - 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상)
    - 프록시 객체는 실제 객체의 참조를 보관한다.
    - 프록시 객체를 호출하면 프록시 객체는 실제 객체의 매소드 호출
  - 프록시 객체의 초기화
  <img width="544" alt="image" src="https://github.com/rlatjsrnr/jpaBasic/assets/137128415/713bbc11-0e77-40f3-8e73-8fcc30a4938c">

    1. 사용자가 프록시 객체에 메소드를 호출하면 프록시 객체는 영속성 컨택스트에 초기화를 요청 함
    2. 영속성 컨텍스트는 해당 엔티티를 데이터베이스에서 조회한다.
    3. 실제 엔티티를 생성한다.
    4. 프록시 객체가 타겟 엔티티의 매소드를 호출한다. 

    - 프록시 객체를 초기화할 떄 프록시 객체가 엔티티로 바뀌는 것은 아니다. 실제 엔티티에 접근 가능해 짐
    - 프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시에 주의가 필요(== 비교시 false, instance of 사용)
    - 만약 영속성 컨텍스트에 찾는 엔티티가 이미 있다면 em.getReference() 를 호출하여도 실제 엔티티 반환
    - 만약 이미 프록시 객체 생성되어 있는데 해당 객체를 em.find() 하면 엔티티가 아닌 프록시 객체를 반환
    - 영속성 컨텍스트의 도움을 받을 수 없는 상태에서 프록시를 초기화하면 예외 발생 (org.hibernate.LazyInitializationException)
  - 프록시 확인
    - 프록시 객체의 초기화 여부 확인 : PersistenceUnitUtil.isLoaded(Object entity)
    - 프록시 클래스 확인 : entity.getClass().getName() 출력
    - 프록시 강제 초기화 : org.hibernate.Hibernate.initialize(entity);
    - JPA표준에는 강제 초기화 없음
  
- 즉시 로딩과 지연 로딩  
  - 즉시 로딩(fetch = FetchType.EAGER) : JPA 구현체는 가능하면 조인을 사용해서 SQL을 한 번에 함께 조회, 조회시 연관된 모든 테이블 조회
  - 지연 로딩(fetch = FetchType.LAZY) : 연관된 테이블이 실제 필요한 시점에 초기화 ( 프록시 사용 )
  - 프록시와 즉시 로딩 주의
    - 가급적 지연 로딩만 사용
    - 즉시 로딩 시 예상치 못한 SQL 발생
    - 즉시 로딩은 JPQL에서 N+1 문제를 발생시킴
    - @ManyToOne, @OneToOne 은 기본이 즉시 로딩이므로 지연 로딩으로 설정
    - @OneToMany, @ManyToMany는 기본이 지연 로딩
- 지연 로딩 활용
  - 모든 연관관계에서 지연 로딩을 활용하자!!!
  - 즉시 로딩의 방식이 필요 하다면 JPQL의 fetch 조인을 활용하자  
- 영속성 전이 : CASCADE
  - 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때 사용한다.
  - ex) 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장
  - 주의 : 영속성 전이는 연관관계 매핑과는 아무런 연관이 없다. 그저 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화 하는 편리함을 제공할 뿐.
  - CASCADE의 종류
    - ALL : 모두 적용
    - PERSIST : 영속
    - REMOVE : 삭제
    - MERGE : 병합
    - REFRESH : REFRESH
    - DETACH : DETACH
    - 주로 ALL 또는 PERSIST 사용      
- 고아 객체
  - 고아 객체 제거 : 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
  - orphanRemoval = true
  - ```java
    Parent parent1 = em.find(Parent.class, id);
    parent1.getChildren().remove(0);
    //자식 엔티티를 컬렉션에서 제거
    ```
  - 위의 경우 0번에 해당하는 자식 객체를 삭제한다.
  - 주의
    - 참조하는 곳이 하나일 때 사용해야 함!!
    - 특정 엔티티가 개인 소유할 때 사용
    - @OneToOne, @OneToMany만 가능
      
- 영속성 전이 + 고아 객체, 생명 주기
  - CascadeType.ALL, orphanRemoval=true 두 속성을 함께 사용하면 부모 엔티티를 통해서 자식 엔티티의 생명주기 관리가 가능하다
  - 부모 객체가 영속화되면 자식 객체도 영속화
  - 부모 객체가 삭제되었을 때, 부모 객체로 부터 연관관계가 끊어졌을 때 모두 자식객체 삭제

