package com.example.tugas1.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.tugas1.model.KecamatanModel;

@Mapper
public interface KecamatanMapper {
	@Select("select * from kecamatan where id = #{id}")
	KecamatanModel selectKecamatanById (@Param("id") BigInteger id);
	
	@Select("select * from kecamatan where id_kota = #{id_kota}")
	List<KecamatanModel> selectAllKecamatanByKotaId (@Param("id_kota") BigInteger id_kota);
}
