package com.generated.qualityTrace.repositories;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generated.qualityTrace.models.SysUser;

@Mapper
public interface SysUserRepository extends BaseMapper<SysUser> {

  @Select("SELECT * FROM sys_user WHERE username = #{username}")
  SysUser selectByUsername(@Param("username") String username);
}
