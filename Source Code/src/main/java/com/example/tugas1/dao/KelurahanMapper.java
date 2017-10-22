package com.example.tugas1.dao;

import java.util.List;
import java.math.BigInteger;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.tugas1.model.KelurahanModel;

@Mapper
public interface KelurahanMapper {
	@Select("select * from kelurahan where id = #{id}")
	KelurahanModel selectKelurahanById (@Param("id") BigInteger id);
	
	@Select("select * from kelurahan")
	List<KelurahanModel> selectAllKelurahan ();
	
	@Select("select * from kelurahan where id_kecamatan = #{id_kecamatan}")
	List<KelurahanModel> selectAllKelurahanByKecamatanId (@Param("id_kecamatan") BigInteger id_kecamatan);
}
