package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer id;
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 4, max = 32,message = "用户名不能小于4位或大于32位")
    private String username;
    @NotNull(message = "密码不能为空")
    @Length(min = 4,max = 32,message = "密码长度不能小于4位或大于32位")
    @JsonIgnore
    private String password;
    @Pattern(regexp = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$" ,message = "手机号格式不正确")
    private String phone;
    private Date created;
    @JsonIgnore
    private String salt;
}
