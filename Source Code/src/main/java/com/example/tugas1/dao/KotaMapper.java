package com.example.tugas1.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.tugas1.model.KotaModel;

@Mapper
public interface KotaMapper {
	@Select("select * from kota where id = #{id}")
	KotaModel selectKotaById (@Param("id") BigInteger id);
	
	@Select("select * from kota")
	List<KotaModel> selectAllKota ();
}
