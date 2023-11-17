package hellojpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Parent {

    @Id @GeneratedValue
    private Long id;
    private String name;

    // 소유자(부모)가 하나 일 때만 사용
    // 둘 다 켜면 자식의 생명주기를 부모가 관리한다.
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();

    public void addChild(Child child){
        childList.add(child);
        child.setParent(this);
    }
}
