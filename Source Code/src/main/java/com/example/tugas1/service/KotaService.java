package com.example.tugas1.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tugas1.dao.KotaMapper;
import com.example.tugas1.model.KotaModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KotaService {
	@Autowired
	private KotaMapper kotaMapper;
	
	public KotaModel selectKotaById (BigInteger id) {
		log.info("KotaService: Select Kota with ID {}", id);
		return kotaMapper.selectKotaById(id);
	}
	
	public List<KotaModel> selectAllKota () {
		log.info("KotaService: Select All Kota");
		return kotaMapper.selectAllKota();
	}
}
