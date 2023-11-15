package hellojpa;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@SequenceGenerator(name = "member_seq_generator", sequenceName = "member_seq", initialValue = 1, allocationSize = 50)
/*@TableGenerator(
        name = "Member_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "MEMBER_SEQ", allocationSize = 1
)
 */
public class Member {

    @Id
    @GeneratedValue(
            //strategy = GenerationType.AUTO // SQL 방언에 따라 자동 생성

            // DB에 위임한다. 설정에 따라 DB에 id값은 insert가 되어야 알 수 있으므로 persist시점에 insert쿼리를 날린다
            //strategy = GenerationType.IDENTITY

            // 데이터베이스 시퀀스 오브젝트 사용 sequence object를 db가 관리하므로 persist 하면 db에서 sequence를 가져옴
            strategy = GenerationType.SEQUENCE, generator = "member_seq_generator"

            //strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR"
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
