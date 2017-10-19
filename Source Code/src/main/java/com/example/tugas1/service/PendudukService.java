package com.example.tugas1.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tugas1.dao.PendudukMapper;

import com.example.tugas1.model.PendudukModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PendudukService {
	@Autowired
	private PendudukMapper pendudukMapper;
	
	public PendudukModel selectPenduduk (String nik) {
		log.info("Select Penduduk with NIK {}", nik);
		return pendudukMapper.selectPenduduk(nik);
	}
	
	public List<PendudukModel> selectPendudukFromKeluarga (BigInteger id_keluarga) {
		log.info("Select Penduduk of Keluarga with NKK {}", id_keluarga);
		return pendudukMapper.selectPendudukFromKeluarga(id_keluarga);
	}
	
	public BigInteger countAllPenduduk () {
		log.info("counting all penduduk");
		return pendudukMapper.countAllPenduduk();
	}
	
	public int countCurrentPendudukOnQuery(String query) {
		log.info("Count current number of penduduk with query {}", query);
		return pendudukMapper.countCurrentPendudukOnQuery(query);
	}
	
	public void addPenduduk (PendudukModel penduduk) {
		log.info("Add Penduduk {}", penduduk);
		pendudukMapper.addPenduduk(penduduk);
	}
	
	public void updatePenduduk (PendudukModel penduduk) {
		log.info("Updating Penduduk {}", penduduk);
		pendudukMapper.updatePenduduk(penduduk);
	}
	
	public void setPendudukWafat (BigInteger id) {
		log.info("Set Penduduk with id {} to Wafat", id);
		pendudukMapper.setPendudukWafat(id);
	}
}
