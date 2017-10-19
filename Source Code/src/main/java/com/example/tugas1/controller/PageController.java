package com.example.tugas1.controller;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.tugas1.model.KecamatanModel;
import com.example.tugas1.model.KeluargaModel;
import com.example.tugas1.model.KelurahanModel;
import com.example.tugas1.model.KotaModel;
import com.example.tugas1.model.PendudukModel;

import com.example.tugas1.service.KecamatanService;
import com.example.tugas1.service.KeluargaService;
import com.example.tugas1.service.KelurahanService;
import com.example.tugas1.service.KotaService;
import com.example.tugas1.service.PendudukService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PageController {
    @Autowired
    KecamatanService kecamatanService;

    @Autowired
    KeluargaService keluargaService;

    @Autowired
    KelurahanService kelurahanService;

    @Autowired
    KotaService kotaService;

    @Autowired
    PendudukService pendudukService;

    @RequestMapping("/")
	public String index () {
		return "index";
	}

    @RequestMapping("/penduduk")
	public String penduduk (
		Model model,
		@RequestParam(value = "nik", required = false) String nik
    ) {
    	if (nik != null) {
    		PendudukModel penduduk = pendudukService.selectPenduduk(nik);
    		log.info("Penduduk {}", penduduk);
    		model.addAttribute("penduduk", penduduk);
    		
    		KeluargaModel keluarga = keluargaService.selectKeluargaById(penduduk.getId_keluarga());
    		model.addAttribute("keluarga", keluarga);
    		
    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
    		model.addAttribute("kelurahan", kelurahan);
    		
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
    		model.addAttribute("kecamatan", kecamatan);
    		
    		KotaModel kota = kotaService.selectKotaById(kecamatan.getId_kota());
    		model.addAttribute("kota", kota);
    		
    		return "penduduk-view";
    	}

		return "penduduk";
	}

    @RequestMapping(value = "/penduduk/tambah", method = RequestMethod.GET)
	public String tambahPenduduk () {
		return "penduduk-add";
	}

    @RequestMapping(value = "/penduduk/tambah", method = RequestMethod.POST)
	public String tambahPendudukSubmit (Model model, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {
    	log.info("request penduduk {}", penduduk);
    	
    	if (result.hasErrors()) {
        	log.info("errornya {}", result.getAllErrors());
    		return "penduduk-add";
    	}
    	
    	if (!penduduk.getTanggal_lahir().matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
    		log.info("ini bodoh");
    		return "penduduk-add";
    	}
    	
    	String[] tahunBulanHari = penduduk.getTanggal_lahir().split("-");
    	int tahun = (Integer.parseInt(tahunBulanHari[0]) % 1000) % 100;
    	int bulan = Integer.parseInt(tahunBulanHari[1]);
    	int hari = Integer.parseInt(tahunBulanHari[2]);
    	
    	if (penduduk.getJenis_kelamin() == 1) {
    		hari += 40;
    	}
		
		KeluargaModel keluarga = keluargaService.selectKeluargaById(penduduk.getId_keluarga());
		
		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
		
		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
		
		String kodeProvinsiKotaKecamatan = kecamatan.getKode_kecamatan().substring(0, 6);

		String prefix = kodeProvinsiKotaKecamatan + String.format("%02d", hari) + String.format("%02d", bulan) + String.format("%02d", tahun);
		String query = prefix + "%";
		
		int localizedCount = pendudukService.countCurrentPendudukOnQuery(query);
		BigInteger allCount = pendudukService.countAllPenduduk();
		
		String finalNik = prefix + String.format("%04d", localizedCount + 1);
		
		penduduk.setNik(finalNik);
		penduduk.setId(allCount.add(new BigInteger("1")));
		
		pendudukService.addPenduduk(penduduk);

		model.addAttribute("penduduk", penduduk);
    	
    	return "penduduk-add-success";
	}

    @RequestMapping(value = "/penduduk/ubah/{nik}", method = RequestMethod.GET)
	public String ubahPenduduk (Model model, @PathVariable(value = "nik") String nik) {
    	PendudukModel penduduk = pendudukService.selectPenduduk(nik);
    	
    	if (penduduk != null) {
    		model.addAttribute("penduduk", penduduk);
    		return "penduduk-edit";
    	}
    	
    	model.addAttribute("nik", nik);
		return "penduduk-not-found";
	}


    @RequestMapping(value = "/penduduk/ubah/{nik}", method = RequestMethod.POST)
	public String ubahPendudukSubmit (Model model, @PathVariable(value = "nik") String nik, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {

    	if (result.hasErrors()) {
    		model.addAttribute("penduduk", penduduk);
    		return "penduduk-edit";
    	}
    	
    	PendudukModel original = pendudukService.selectPenduduk(nik);
    	
    	if (
    		penduduk.getTanggal_lahir() != original.getTanggal_lahir() ||
    		penduduk.getId_keluarga().compareTo(original.getId_keluarga()) != 0 ||
    		penduduk.getJenis_kelamin() != original.getJenis_kelamin()
    	) {
        	String[] tahunBulanHari = penduduk.getTanggal_lahir().split("-");
        	int tahun = (Integer.parseInt(tahunBulanHari[0]) % 1000) % 100;
        	int bulan = Integer.parseInt(tahunBulanHari[1]);
        	int hari = Integer.parseInt(tahunBulanHari[2]);
        	
        	if (penduduk.getJenis_kelamin() == 1) {
        		hari += 40;
        	}
    		
    		KeluargaModel keluarga = keluargaService.selectKeluargaById(penduduk.getId_keluarga());
    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
    		
    		String kodeProvinsiKotaKecamatan = kecamatan.getKode_kecamatan().substring(0, 6);

    		String prefix = kodeProvinsiKotaKecamatan + String.format("%02d", hari) + String.format("%02d", bulan) + String.format("%02d", tahun);
    		String query = prefix + "%";
    		
    		int localizedCount = pendudukService.countCurrentPendudukOnQuery(query);
    		
    		String finalNik = prefix + String.format("%04d", localizedCount + 1);
    		
    		penduduk.setNik(finalNik);
    	}
    	
    	pendudukService.updatePenduduk(penduduk);

		model.addAttribute("nik", nik);
		model.addAttribute("penduduk", penduduk);
		return "penduduk-edit-success";
	}


    @RequestMapping(value = "/penduduk/mati/", method = RequestMethod.POST)
	public String setPendudukWafat (Model model, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {
    	log.info("deleting penduduk {}", penduduk);
    	penduduk.setIs_wafat(1);

		KeluargaModel keluarga = keluargaService.selectKeluargaById(penduduk.getId_keluarga());
		log.info("Keluarga {}", keluarga);
		
		List<PendudukModel> anggota = pendudukService.selectPendudukFromKeluarga(keluarga.getId());
		log.info("Anggota {}", anggota);
		
		boolean isAllDead = true;
		
		for (int i = 0; i < anggota.size(); i++) {
			log.info("current anggota {}", anggota.get(i));
			if (anggota.get(i).getIs_wafat() == 0 && anggota.get(i).getId().compareTo(penduduk.getId()) != 0) {
				log.info("not all dead, anggota {} is alive dumb fuck", anggota.get(i));
				isAllDead = false;
			}
		}
		
		if (isAllDead) {
			log.info("all ded");
			keluarga.setIs_tidak_berlaku(1);
			keluargaService.updateKeluarga(keluarga);
		}
    	
    	pendudukService.updatePenduduk(penduduk);

		model.addAttribute("penduduk", penduduk);
		return "penduduk-nonaktifkan-sukses";
	}

    @RequestMapping("/keluarga")
	public String keluarga (
		Model model,
		@RequestParam(value = "nkk", required = false) String nkk
	) {
    	if (nkk != null) {
    		KeluargaModel keluarga = keluargaService.selectKeluarga(nkk);
    		log.info("Keluarga {}", keluarga);
    		model.addAttribute("keluarga", keluarga);
    		
    		List<PendudukModel> anggota = pendudukService.selectPendudukFromKeluarga(keluarga.getId());
    		log.info("Anggota {}", anggota);
    		model.addAttribute("anggota", anggota);

    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
    		model.addAttribute("kelurahan", kelurahan);
    		
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
    		model.addAttribute("kecamatan", kecamatan);
    		
    		KotaModel kota = kotaService.selectKotaById(kecamatan.getId_kota());
    		model.addAttribute("kota", kota);
    		
    		return "keluarga-view";
    	}

		return "keluarga";
	}

    @RequestMapping(value = "/keluarga/tambah", method = RequestMethod.GET)
	public String tambahKeluarga (Model model) {
    	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
    	model.addAttribute("daftarKelurahan", daftarKelurahan);
    	
		return "keluarga-add";
	}

    @RequestMapping(value = "/keluarga/tambah", method = RequestMethod.POST)
	public String tambahKeluargaSubmit (Model model, @Valid @ModelAttribute KeluargaModel keluarga, BindingResult result) {
    	if (result.hasErrors()) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		return "keluarga-add";
    	}
    	
    	if (keluarga.getRt().length() != 3 || keluarga.getRw().length() != 3) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		return "keluarga-add";
    	}
    	
    	String hariBulanTahun = new SimpleDateFormat("ddMMyy").format(Calendar.getInstance().getTime());
		
		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
		
		String kodeProvinsiKotaKecamatan = kecamatan.getKode_kecamatan().substring(0, 6);

		String prefix = kodeProvinsiKotaKecamatan + hariBulanTahun;
		String query = prefix + "%";
		
		int localizedCount = keluargaService.countCurrentKeluargaOnQuery(query);
		BigInteger allCount = keluargaService.countAllKeluarga();
		
		String finalNkk = prefix + String.format("%04d", localizedCount + 1);
		
		keluarga.setNomor_kk(finalNkk);
		keluarga.setId(allCount.add(new BigInteger("1")));
		
		keluargaService.addKeluarga(keluarga);
		model.addAttribute("keluarga", keluarga);
		return "keluarga-add-success";
	}

    @RequestMapping(value = "/keluarga/ubah/{nkk}", method = RequestMethod.GET)
	public String ubahKeluarga (Model model, @PathVariable(value = "nkk") String nkk) {
    	KeluargaModel keluarga = keluargaService.selectKeluarga(nkk);
    	
    	if (keluarga != null) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
    		return "keluarga-edit";
    	}
    	
    	model.addAttribute("nkk", nkk);
		return "keluarga-not-found";
	}


    @RequestMapping(value = "/keluarga/ubah/{nkk}", method = RequestMethod.POST)
	public String ubahKeluargaSubmit (Model model, @PathVariable(value = "nkk") String nkk, @Valid @ModelAttribute KeluargaModel keluarga, BindingResult result) {
    	KeluargaModel original = keluargaService.selectKeluarga(nkk);

    	if (result.hasErrors()) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
    		return "keluarga-edit";
    	}
    	
    	if (keluarga.getRt().length() != 3 || keluarga.getRw().length() != 3) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
    		return "keluarga-edit";
    	}

    	if (keluarga.getId_kelurahan().compareTo(original.getId_kelurahan()) != 0) {
    		log.info("is this stupid thing being called?");
    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
    		
    		String kodeProvinsiKotaKecamatan = kecamatan.getKode_kecamatan().substring(0, 6);

    		String prefix = kodeProvinsiKotaKecamatan + keluarga.getNomor_kk().substring(6, 12);
    		String query = prefix + "%";
    		
    		int localizedCount = keluargaService.countCurrentKeluargaOnQuery(query);
    		
    		String finalNkk = prefix + String.format("%04d", localizedCount + 1);
    		
    		keluarga.setNomor_kk(finalNkk);
    		
    		List<PendudukModel> anggota = pendudukService.selectPendudukFromKeluarga(keluarga.getId());
    		
    		for (int i = 0; i < anggota.size(); i++) {
    			PendudukModel penduduk = anggota.get(i);
    			log.info("penduduk iterated on {}", penduduk);
 
            	String[] tahunBulanHari = penduduk.getTanggal_lahir().split("-");
            	int tahun = (Integer.parseInt(tahunBulanHari[0]) % 1000) % 100;
            	int bulan = Integer.parseInt(tahunBulanHari[1]);
            	int hari = Integer.parseInt(tahunBulanHari[2]);
            	
            	if (penduduk.getJenis_kelamin() == 1) {
            		hari += 40;
            	}
            	
        		String prefixPenduduk = kodeProvinsiKotaKecamatan + String.format("%02d", hari) + String.format("%02d", bulan) + String.format("%02d", tahun);
        		String queryPenduduk = prefixPenduduk + "%";
        		
        		int localizedCountPenduduk = pendudukService.countCurrentPendudukOnQuery(queryPenduduk);
        		
        		String finalNik = prefixPenduduk + String.format("%04d", localizedCountPenduduk + 1);
        		
        		penduduk.setNik(finalNik);
            	pendudukService.updatePenduduk(penduduk);
    		}
    	}
    	
    	keluargaService.updateKeluarga(keluarga);

		model.addAttribute("nkk", nkk);
		model.addAttribute("keluarga", keluarga);
		return "keluarga-edit-success";
	}


    @RequestMapping("/penduduk/cari")
	public String cariPenduduk (
		Model model,
		@RequestParam(value = "kt", required = false) BigInteger kt,
		@RequestParam(value = "kc", required = false) BigInteger kc,
		@RequestParam(value = "kl", required = false) BigInteger kl
	) throws ParseException {
    	List<KotaModel> listKota = kotaService.selectAllKota();
    	model.addAttribute("listKota", listKota);
    	
    	if (kt != null) {
    		model.addAttribute("kt", kt);
    		List<KecamatanModel> listKecamatan = kecamatanService.selectAllKecamatanByKotaId(kt);
    		model.addAttribute("listKecamatan", listKecamatan);
    	}
    	
    	if (kc != null) {
    		model.addAttribute("kc", kc);
    		List<KelurahanModel> listKelurahan = kelurahanService.selectAllKelurahanByKecamatanId(kc);
    		model.addAttribute("listKelurahan", listKelurahan);
    	}

    	if (kt != null && kc != null && kl != null) {
    		KotaModel kota = kotaService.selectKotaById(kt);
    		model.addAttribute("kota", kota);
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kc);
    		model.addAttribute("kecamatan", kecamatan);
    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(kl);
    		model.addAttribute("kelurahan", kelurahan);
    		
    		List<KeluargaModel> listKeluarga = keluargaService.selectAllKeluargaByKelurahanId(kl);
    		
    		List<PendudukModel> daftarPenduduk = new ArrayList<PendudukModel>();
    		
    		for (int i = 0; i < listKeluarga.size(); i++) {
    			List<PendudukModel> anggotaKeluarga = pendudukService.selectPendudukFromKeluarga(listKeluarga.get(i).getId());
    			daftarPenduduk.addAll(anggotaKeluarga);
    		}
    		
    		PendudukModel youngest = null;
    		PendudukModel oldest = null;
    		
    		for (int i = 0; i < daftarPenduduk.size(); i++) {
    			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    			PendudukModel current = daftarPenduduk.get(i);

    			if (youngest == null) {
    				youngest = daftarPenduduk.get(i);
    			} else {
    				Date youngestBirthDate = dateFormatter.parse(youngest.getTanggal_lahir());
    				Date currentBirthDate = dateFormatter.parse(current.getTanggal_lahir());
    				
    				if (youngestBirthDate.compareTo(currentBirthDate) > 0) {
    					youngest = current;
    				}
    			}
    			
    			if (oldest == null) {
    				oldest = daftarPenduduk.get(i);
    			} else {
    				Date oldestBirthDate = dateFormatter.parse(oldest.getTanggal_lahir());
    				Date currentBirthDate = dateFormatter.parse(current.getTanggal_lahir());
    				
    				if (oldestBirthDate.compareTo(currentBirthDate) < 0) {
    					oldest = current;
    				}
    			}
    		}
    		
    		if (youngest != null) {
    			model.addAttribute("youngest", youngest);
    		}
    		
    		if (oldest != null) {
    			model.addAttribute("oldest", oldest);
    		}
    		
    		model.addAttribute("daftarPenduduk", daftarPenduduk);
    		
    		return "cari-result";
    	}
    	
		return "cari";
	}
}
