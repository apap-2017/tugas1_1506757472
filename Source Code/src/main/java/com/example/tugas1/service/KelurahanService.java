package com.example.tugas1.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tugas1.dao.KelurahanMapper;
import com.example.tugas1.model.KelurahanModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KelurahanService {
	@Autowired
	private KelurahanMapper kelurahanMapper;
	
	public KelurahanModel selectKelurahanById (BigInteger id) {
		log.info("KelurahanService: Select Kelurahan with ID {}", id);
		return kelurahanMapper.selectKelurahanById(id);
	}
	
	public List<KelurahanModel> selectAllKelurahan () {
		log.info("KelurahanService: Select All Kelurahan");
		return kelurahanMapper.selectAllKelurahan();
	}
	
	public List<KelurahanModel> selectAllKelurahanByKecamatanId (BigInteger id_kecamatan) {
		log.info("KelurahanService: Select All Kelurahan from Kecamatan with ID {}", id_kecamatan);
		return kelurahanMapper.selectAllKelurahanByKecamatanId(id_kecamatan);
	}
}
