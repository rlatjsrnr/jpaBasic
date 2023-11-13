package hellojpa;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
//@SequenceGenerator(name = "member_seq_generator", sequenceName = "member_seq")
@TableGenerator(
        name = "Member_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "MEMBER_SEQ", allocationSize = 1
)
public class Member {

    @Id
    @GeneratedValue(
            //strategy = GenerationType.AUTO // SQL 방언에 따라 자동 생성
            //strategy = GenerationType.IDENTITY // DB에 위임한다. 설정에 따라
            //strategy = GenerationType.SEQUENCE, generator = "member_seq_generator" // 데이터베이스 시퀀스 오브젝트 사용
            strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR"
    )
    private Long id;
    @Column(name="name", nullable = false)
    private String username;

    public Member() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
