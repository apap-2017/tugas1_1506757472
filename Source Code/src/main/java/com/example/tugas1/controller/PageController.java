package com.example.tugas1.controller;

import java.math.BigInteger;
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
import org.springframework.validation.ObjectError;
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
    	log.info("Req: Index");
		return "index";
	}

    @RequestMapping("/penduduk")
	public String penduduk (
		Model model,
		@RequestParam(value = "nik", required = false) String nik
    ) {
    	if (nik != null) {
    		PendudukModel penduduk = pendudukService.selectPenduduk(nik);

    		if (penduduk != null) {
        		log.info("Req: View Penduduk {}", penduduk.getId());
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
    		
    		log.info("ERR! Penduduk Not Found");
    		return "error/penduduk-not-found";
    	}
		
		log.info("Req: Index Penduduk");
		return "penduduk";
	}

    @RequestMapping(value = "/penduduk/tambah", method = RequestMethod.GET)
	public String tambahPenduduk () {
    	log.info("Req: Tambah Penduduk");
		return "penduduk-add";
	}

    @RequestMapping(value = "/penduduk/tambah", method = RequestMethod.POST)
	public String tambahPendudukSubmit (Model model, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {
    	log.info("Req: POST Tambah Penduduk {}", penduduk);
    	
    	if (result.hasErrors()) {
    		List<ObjectError> resultErrors = result.getAllErrors();

    		for (int i = 0; i < resultErrors.size(); i++) {
    			model.addAttribute("error" + resultErrors.get(i).getCodes()[0].split("pendudukModel")[1].substring(1), resultErrors.get(i).getDefaultMessage());
    		}
    		
    		log.info("ERR! POST Tambah Penduduk Fail, validation error");
    		model.addAttribute("penduduk", penduduk);
    		return "penduduk-add";
    	}
    	
    	if (!penduduk.getTanggal_lahir().matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
    		log.info("ERR! POST Tambah Penduduk Fail, tanggal_lahir error");
    		model.addAttribute("errortanggal_lahir", "Format Tanggal Lahir harus YYYY-MM-DD");
    		model.addAttribute("penduduk", penduduk);
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
		
		if (keluarga.getIs_tidak_berlaku() == 1) {
			log.info("Keluarga {} is set to be active because there is new member", keluarga.getId());
			keluargaService.setKeluargaActive(keluarga.getId());
		}

		model.addAttribute("penduduk", penduduk);
    	
		log.info("SUC! POST Tambah Penduduk {} Success", penduduk.getId());
    	return "penduduk-add-success";
	}

    @RequestMapping(value = "/penduduk/ubah/{nik}", method = RequestMethod.GET)
	public String ubahPenduduk (Model model, @PathVariable(value = "nik") String nik) {
    	PendudukModel penduduk = pendudukService.selectPenduduk(nik);
    	
    	if (penduduk != null) {
    		model.addAttribute("penduduk", penduduk);
    		log.info("Req: Ubah Penduduk");
    		return "penduduk-edit";
    	}

		log.info("ERR! Penduduk Not Found");
		return "penduduk-not-found";
	}


    @RequestMapping(value = "/penduduk/ubah/{nik}", method = RequestMethod.POST)
	public String ubahPendudukSubmit (Model model, @PathVariable(value = "nik") String nik, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {    	
		log.info("Req: POST Ubah Penduduk {}", penduduk.getId());

    	if (result.hasErrors()) {
    		log.info("ERR! POST Ubah Penduduk Failed, validation error");
    		List<ObjectError> resultErrors = result.getAllErrors();

    		for (int i = 0; i < resultErrors.size(); i++) {
    			model.addAttribute("error" + resultErrors.get(i).getCodes()[0].split("pendudukModel")[1].substring(1), resultErrors.get(i).getDefaultMessage());
    		}

    		model.addAttribute("penduduk", penduduk);
    		return "penduduk-edit";
    	}
    	
    	if (!penduduk.getTanggal_lahir().matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
    		log.info("ERR! POST Ubah Penduduk Failed, tanggal_lahir error");
    		model.addAttribute("errortanggal_lahir", "Format Tanggal Lahir harus YYYY-MM-DD");
    		model.addAttribute("penduduk", penduduk);
    		return "penduduk-edit";
    	}
    	
    	PendudukModel original = pendudukService.selectPenduduk(nik);
    	
    	if (
    		penduduk.getTanggal_lahir() != original.getTanggal_lahir() ||
    		penduduk.getId_keluarga().compareTo(original.getId_keluarga()) != 0 ||
    		penduduk.getJenis_kelamin() != original.getJenis_kelamin()
    	) {
    		log.info("There has been changes in field that has to do with NIK, generating new NIK");
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
    		log.info("New NIK has been generated for Penduduk {} with new NIK {}", penduduk.getId(), finalNik);
    	}
    	
    	pendudukService.updatePenduduk(penduduk);

		model.addAttribute("nik", nik);
		model.addAttribute("penduduk", penduduk);    	
		log.info("SUC! POST Ubah Penduduk {} Success", penduduk.getId());
		return "penduduk-edit-success";
	}


    @RequestMapping(value = "/penduduk/mati/", method = RequestMethod.POST)
	public String setPendudukWafat (Model model, @Valid @ModelAttribute PendudukModel penduduk, BindingResult result) {
    	log.info("Setting Penduduk {} status to Dead", penduduk.getId());
    	penduduk.setIs_wafat(1);

    	log.info("Checking Keluarga Active Status");
		KeluargaModel keluarga = keluargaService.selectKeluargaById(penduduk.getId_keluarga());
		
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
			keluarga.setIs_tidak_berlaku(1);
			log.info("Setting Keluarga {} Status to Inactive Because All Family Member has Died", keluarga.getId());
			keluargaService.updateKeluarga(keluarga);
		}
    	
    	pendudukService.updatePenduduk(penduduk);

		model.addAttribute("penduduk", penduduk);
		log.info("Penduduk {} Has been Set to Dead", penduduk.getId());
		return "penduduk-nonaktifkan-sukses";
	}

    @RequestMapping("/keluarga")
	public String keluarga (
		Model model,
		@RequestParam(value = "nkk", required = false) String nkk
	) {
    	if (nkk != null) {
    		KeluargaModel keluarga = keluargaService.selectKeluarga(nkk);
    		
    		if (keluarga != null) {
    			log.info("Req: View Keluarga {}", keluarga.getId());
	    		model.addAttribute("keluarga", keluarga);
	    		
	    		List<PendudukModel> anggota = pendudukService.selectPendudukFromKeluarga(keluarga.getId());
	    		model.addAttribute("anggota", anggota);
	
	    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
	    		model.addAttribute("kelurahan", kelurahan);
	    		
	    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
	    		model.addAttribute("kecamatan", kecamatan);
	    		
	    		KotaModel kota = kotaService.selectKotaById(kecamatan.getId_kota());
	    		model.addAttribute("kota", kota);
	    		
	    		return "keluarga-view";
    		}
    		
    		log.info("Err! Keluarga Not Found");
    		return "error/keluarga-not-found";
    	}

    	log.info("Req: Index Keluarga");
		return "keluarga";
	}

    @RequestMapping(value = "/keluarga/tambah", method = RequestMethod.GET)
	public String tambahKeluarga (Model model) {
    	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
    	model.addAttribute("daftarKelurahan", daftarKelurahan);
    	
    	log.info("Req: Tambah Keluarga");
		return "keluarga-add";
	}

    @RequestMapping(value = "/keluarga/tambah", method = RequestMethod.POST)
	public String tambahKeluargaSubmit (Model model, @Valid @ModelAttribute KeluargaModel keluarga, BindingResult result) {
    	log.info("Req: POST Tambah Keluarga {}", keluarga);

    	if (result.hasErrors()) {
    		log.info("ERR! POST Tambah Keluarga fail, validation error");
    		List<ObjectError> resultErrors = result.getAllErrors();

    		for (int i = 0; i < resultErrors.size(); i++) {
    			model.addAttribute("error" + resultErrors.get(i).getCodes()[0].split("keluargaModel")[1].substring(1), resultErrors.get(i).getDefaultMessage());
    		}
    		
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("keluarga", keluarga);
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		return "keluarga-add";
    	}
    	
    	if (keluarga.getRt().length() != 3 || keluarga.getRw().length() != 3) {
    		log.info("ERR! POST Tambah Keluarga fail, rt/rw error");
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
		log.info("SUC! POST Tambah Keluarga {} Success", keluarga.getId());
		return "keluarga-add-success";
	}

    @RequestMapping(value = "/keluarga/ubah/{nkk}", method = RequestMethod.GET)
	public String ubahKeluarga (Model model, @PathVariable(value = "nkk") String nkk) {
    	KeluargaModel keluarga = keluargaService.selectKeluarga(nkk);
    	
    	if (keluarga != null) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
        	log.info("Req: Ubah Keluarga");
    		return "keluarga-edit";
    	}

    	log.info("ERR! Keluarga Not Found");
		return "keluarga-not-found";
	}


    @RequestMapping(value = "/keluarga/ubah/{nkk}", method = RequestMethod.POST)
	public String ubahKeluargaSubmit (Model model, @PathVariable(value = "nkk") String nkk, @Valid @ModelAttribute KeluargaModel keluarga, BindingResult result) {
    	log.info("Req: POST Ubah Keluarga {}", keluarga.getId());
    	KeluargaModel original = keluargaService.selectKeluarga(nkk);

    	if (result.hasErrors()) {
    		List<ObjectError> resultErrors = result.getAllErrors();

    		for (int i = 0; i < resultErrors.size(); i++) {
    			model.addAttribute("error" + resultErrors.get(i).getCodes()[0].split("keluargaModel")[1].substring(1), resultErrors.get(i).getDefaultMessage());
    		}
    		
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
    		log.info("ERR! POST Ubah Keluarga Failed, validation error");
    		return "keluarga-edit";
    	}
    	
    	if (keluarga.getRt().length() != 3 || keluarga.getRw().length() != 3) {
        	List<KelurahanModel> daftarKelurahan = kelurahanService.selectAllKelurahan();
        	model.addAttribute("daftarKelurahan", daftarKelurahan);
    		model.addAttribute("keluarga", keluarga);
    		log.info("ERR! POST Ubah Keluarga Failed, rt/rw error");
    		return "keluarga-edit";
    	}

    	if (keluarga.getId_kelurahan().compareTo(original.getId_kelurahan()) != 0) {
    		log.info("ID Kelurahan has been Changed, New NKK has to be generated and All Keluarga Member NIK has to be Updated");
    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(keluarga.getId_kelurahan());
    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kelurahan.getId_kecamatan());
    		
    		String kodeProvinsiKotaKecamatan = kecamatan.getKode_kecamatan().substring(0, 6);

    		String prefix = kodeProvinsiKotaKecamatan + keluarga.getNomor_kk().substring(6, 12);
    		String query = prefix + "%";
    		
    		int localizedCount = keluargaService.countCurrentKeluargaOnQuery(query);
    		
    		String finalNkk = prefix + String.format("%04d", localizedCount + 1);
    		
    		keluarga.setNomor_kk(finalNkk);
    		log.info("New NKK has been generated for Keluarga {} with new NKK {}", keluarga.getId(), finalNkk);
    		
    		List<PendudukModel> anggota = pendudukService.selectPendudukFromKeluarga(keluarga.getId());
    		
    		for (int i = 0; i < anggota.size(); i++) {
    			PendudukModel penduduk = anggota.get(i);
        		log.info("Generating New NIK for Penduduk {} Because Penduduk Keluarga Has Changed its Kelurahan", penduduk.getId());
 
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
        		log.info("New NIK has been generated for Penduduk {} with new NIK {}", penduduk.getId(), finalNik);
    		}
    	}
    	
    	keluargaService.updateKeluarga(keluarga);

		model.addAttribute("nkk", nkk);
		model.addAttribute("keluarga", keluarga);
		log.info("SUC! POST Ubah Keluarga {} Success", keluarga.getId());
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
    		List<KecamatanModel> listKecamatan = kecamatanService.selectAllKecamatanByKotaId(kt);
    		
    		if (listKecamatan.size() > 0) {
        		model.addAttribute("kt", kt);
        		model.addAttribute("listKecamatan", listKecamatan);
        		log.info("Kota {} has been provided by link parameter, containing List of Kecamatan {}", kt, listKecamatan);
    		} else {
    			log.info("Given Kota {} is not valid", kt);
    			return "error/cari-not-found";
    		}
    	}
    	
    	if (kc != null) {
    		List<KelurahanModel> listKelurahan = kelurahanService.selectAllKelurahanByKecamatanId(kc);
    		
    		if (listKelurahan.size() > 0) {
        		model.addAttribute("kc", kc);
        		model.addAttribute("listKelurahan", listKelurahan);
        		log.info("Kecamatan {} has been provided by link parameter, containing List of Kelurahan {}", kc, listKelurahan);
    		} else {
    			log.info("Given Kecamatan {} is not valid", kc);
    			return "error/cari-not-found";
    		}
    	}

    	if (kt != null && kc != null && kl != null) {
    		KotaModel kota = kotaService.selectKotaById(kt);
    		
    		if (kota != null) {
	    		KecamatanModel kecamatan = kecamatanService.selectKecamatanById(kc);
	    		
	    		if (kecamatan != null) {
		    		KelurahanModel kelurahan = kelurahanService.selectKelurahanById(kl);
		    		
		    		if (kelurahan != null) {
		    			if (
		    				!kelurahan.getId_kecamatan().equals(kecamatan.getId()) ||
		    				!kecamatan.getId_kota().equals(kota.getId())
		    			) {
		        			log.info("ERR! Given Combination of Kota {}, Kecamatan {}, and Kelurahan {} is not valid", kt, kc, kl);
		    				return "error/cari-not-found";
		    			}
		    			
		    			log.info("Req: Cari for Penduduk in Kelurahan {}, Kecamatan {}, Kota {}", kl, kc, kt);
		        		model.addAttribute("kota", kota);
			    		model.addAttribute("kecamatan", kecamatan);
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
			    				
			    				if (youngestBirthDate.compareTo(currentBirthDate) < 0) {
			    					youngest = current;
			    				}
			    			}
			    			
			    			if (oldest == null) {
			    				oldest = daftarPenduduk.get(i);
			    			} else {
			    				Date oldestBirthDate = dateFormatter.parse(oldest.getTanggal_lahir());
			    				Date currentBirthDate = dateFormatter.parse(current.getTanggal_lahir());
			    				
			    				if (oldestBirthDate.compareTo(currentBirthDate) > 0) {
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
			    		
			    		log.info("SUC! Finished Querying Results for Penduduk in Kelurahan {}, Kecamatan {}, Kota {} successfully", kl, kc, kt);
			    		return "cari-result";
		    		} else {
		    			log.info("ERR! Given Kelurahan {} is not valid", kl);
		    		}
	    		} else {
	    			log.info("ERR! Given Kecamatan {} is not valid", kc);
	    		}
    		} else {
    			log.info("ERR! Given Kota {} is not valid", kt);
    		}
    		
    		return "error/cari-not-found";
    	}
    	
    	log.info("Req: Cari");    
		return "cari";
	}
}
