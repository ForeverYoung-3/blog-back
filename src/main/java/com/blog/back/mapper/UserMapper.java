package com.blog.back.mapper;

import com.blog.back.entity.User;
import com.blog.back.enums.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    /** 插入用户，自动回填 id */
    void insert(User user);

    /** 按 id 更新（只更新非 null 字段） */
    void updateById(User user);

    /** 按 id 删除 */
    void deleteById(@Param("id") Long id);

    /** 按 id 查询 */
    Optional<User> findById(@Param("id") Long id);

    /** 按用户名查询 */
    Optional<User> findByUsername(@Param("username") String username);

    /** 按邮箱查询 */
    Optional<User> findByEmail(@Param("email") String email);

    /** 查询所有用户（按创建时间倒序） */
    List<User> findAll();

    /** 按状态查询用户列表 */
    List<User> findByStatus(@Param("status") UserStatus status);

    /** 用户名是否存在 */
    boolean existsByUsername(@Param("username") String username);

    /** 邮箱是否存在 */
    boolean existsByEmail(@Param("email") String email);

    /** 统计总用户数 */
    long count();

    /** 统计某用户的文章数 */
    long countPostsByUserId(@Param("userId") Long userId);
}
