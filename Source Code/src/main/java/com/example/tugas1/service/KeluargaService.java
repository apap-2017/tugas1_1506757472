package com.example.tugas1.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tugas1.dao.KeluargaMapper;
import com.example.tugas1.model.KeluargaModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KeluargaService {
	@Autowired
	private KeluargaMapper keluargaMapper;
	
	public KeluargaModel selectKeluarga (String nkk) {
		log.info("Select Keluarga with ID {}", nkk);
		return keluargaMapper.selectKeluarga(nkk);
	}
	
	public KeluargaModel selectKeluargaById (BigInteger id) {
		log.info("Select Keluarga with ID {}", id);
		return keluargaMapper.selectKeluargaById(id);
	}
	
	public List<KeluargaModel> selectAllKeluargaByKelurahanId (BigInteger id_kelurahan) {
		log.info("Select All Keluarga from Kelurahan with ID {}", id_kelurahan);
		return keluargaMapper.selectAllKeluargaByKelurahanId(id_kelurahan);
	}
	
	public BigInteger countAllKeluarga () {
		log.info("Counting All Keluarga");
		return keluargaMapper.countAllKeluarga();
	}
	
	public int countCurrentKeluargaOnQuery (String query) {
		log.info("Counting Current Ammount of Keluarga with Query {}", query);
		return keluargaMapper.countCurrentKeluargaOnQuery(query);
	}
	
	public void addKeluarga (KeluargaModel keluarga) {
		log.info("Adding new Keluarga with data {}", keluarga);
		keluargaMapper.addKeluarga(keluarga);
	}
	
	public void updateKeluarga (KeluargaModel keluarga) {
		log.info("Updating Keluarga with data {}", keluarga);
		keluargaMapper.updateKeluarga(keluarga);
	}
	
	public void setKeluargaActive (BigInteger id) {
		log.info("Set Keluarga with id {} to Active", id);
		keluargaMapper.setKeluargaActive(id);
	}
	
	public void setKeluargaInactive (BigInteger id) {
		log.info("Set Keluarga with id {} to Inactive", id);
		keluargaMapper.setKeluargaInactive(id);
	}
}
