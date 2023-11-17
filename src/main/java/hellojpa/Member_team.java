package hellojpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity @Getter @Setter
public class Member_team extends BaseEntity{
    @Id @GeneratedValue
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    /*@Column(name = "TEAM_ID")
    private Long teamId;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    // 연관관계 편의 매소드
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
