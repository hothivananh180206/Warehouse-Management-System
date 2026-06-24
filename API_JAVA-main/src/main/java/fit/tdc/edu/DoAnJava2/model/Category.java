package fit.tdc.edu.DoAnJava2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true) // Tên danh mục không được trống và không được trùng
    private String name;

    public Category() {
        super();
    }

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}