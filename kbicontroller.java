package com.example.b04.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.b04.model.IndikatorKBI;
import com.example.b04.model.JawabanKBI;
import com.example.b04.model.KBI;
import com.example.b04.model.KBI_Karyawan;
import com.example.b04.model.Karyawan;
import com.example.b04.service.KBIService;

@Controller
public class KBIController {
	
	@Autowired
    KBIService kbiDAO;
	
	public String isRoleValid(Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		} else {	
			return "ada";
		}
	}
	
	public String getRole (HttpSession session) {
		String namaRole = session.getAttribute("currentRole").toString();
		return namaRole;
	}
	public String getNIK () {
		String nik = SecurityContextHolder.getContext().getAuthentication().getName();
		return nik;
		 
	}
	
	public boolean isStaf(String nik) {
		List<String> stoStaf = kbiDAO.selectStaf();
		System.out.println("masuk SINIII");
		String sto = kbiDAO.selectSto(nik);
		System.out.println(sto);
		
		for (int i = 0; i < stoStaf.size(); i++) {
			if (sto.equals(stoStaf.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	
	@RequestMapping("/KBI/Create")
	public String makeKBI (Principal principal, Model model, HttpSession session) {
	
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("HC HO")) {
			return "redirect:/login"; 
		}
		
		boolean manager = false;
		boolean non_manager = false;
		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
	
		for (int i = 0; i < kbiByPeriode.size(); i++ ) {
			if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
				manager = true;
			} else {
				non_manager = true;
			}
		} 
		if (!manager || !non_manager) {
			model.addAttribute("doneBoth", false);
		}
		
		if (manager) {
			model.addAttribute("non_manager", non_manager);
		} else if (non_manager) {
			model.addAttribute("manager", manager);
		} else {
			model.addAttribute("doneNone", true);
		}
		
		return "KBI-DraftForm";
	}
	
	@RequestMapping("/KBI/Create/Submit")
	public String makeKBISubmit (Model model,
			@RequestParam(value = "jabatan", required = true) String jabatan,
	        @RequestParam(value = "judul", required = true) List<String> judul,
	        @RequestParam(value = "deskripsi", required = true) List<String> deskripsi,
	        @RequestParam(value = "bobot", required = true) List<String> bobot) {
	
		System.out.println(jabatan);
		System.out.println(judul);
		System.out.println(deskripsi);
		System.out.println(bobot);
		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		KBI kbi = new KBI ();
		kbi.setId_periode_penilaian(id_periode_penilaian);
		kbi.setJabatan(jabatan);
		kbiDAO.makeKBI(kbi);
		kbiDAO.addLog(getNIK(), "Create KBI");
		KBI kbiBaru = kbiDAO.selectKBIByNewest();
		String id_kbi = kbiBaru.getId();
		makeIndikatorKBI(id_kbi, judul, deskripsi, bobot);
		return "redirect:/KBI/Edit/Selection";
	}
	
	public void makeIndikatorKBI (String id_kbi, List<String> judul, List<String> deskripsi, List<String> bobot) {
		int jumlahIndikator = judul.size()-1;
		for (int i = 0; i < jumlahIndikator; i++) {
			IndikatorKBI indikator = new IndikatorKBI();
			indikator.setId_kbi(id_kbi);
			indikator.setJudul(judul.get(i));
			indikator.setDeskripsi(deskripsi.get(i));
			indikator.setBobot(Double.parseDouble(bobot.get(i)));
			kbiDAO.makeIndikatorKBI(indikator);
			kbiDAO.addLog(getNIK(), "Create Indikator KBI");
		}
	}

	@RequestMapping("/KBI/Edit/Selection")
	public String editKBISelection (Principal principal, Model model, HttpSession session) {
		
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("HC HO")) {
			return "redirect:/login"; 
		}
		//get list of kbi by periode
		//model isinya list of kbi
		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
		model.addAttribute("kbi", kbiByPeriode);
		return "KBI-DraftFormEditSelection";
	}
	
	@RequestMapping("/KBI/Edit/Selection/{id}")
	public String editKBI (Model model, @PathVariable(value = "id") String id) {
		
		//get kbi by id
		//model isinya listof indikatorkbi
		KBI kbi = kbiDAO.getKBIById(id);
		System.out.println(kbi);
		List<IndikatorKBI> indikator = kbi.getIndikator();
		System.out.println(indikator);
		String jabatan = kbi.getJabatan();
		model.addAttribute("jabatan", jabatan);
		model.addAttribute("indikator", indikator);
		model.addAttribute("id_kbi", id);
		return "KBI-DraftFormEdit";
	}
	
	@RequestMapping("/KBI/Edit/Add/Submit")
	public String editKBIAddSubmit (
			@RequestParam(value = "id_kbi", required = true) String id_kbi,
	        @RequestParam(value = "judul", required = true) String judul,
	        @RequestParam(value = "deskripsi", required = true) String deskripsi,
	        @RequestParam(value = "bobot", required = true) String bobot) {
		IndikatorKBI indikator = new IndikatorKBI();
		indikator.setId_kbi(id_kbi);
		indikator.setJudul(judul);
		indikator.setDeskripsi(deskripsi);
		indikator.setBobot(Double.parseDouble(bobot));
		kbiDAO.makeIndikatorKBI(indikator);
		kbiDAO.addLog(getNIK(), "Create Indikator KBI");
		return "redirect:/KBI/Edit/Selection/"+id_kbi; 
		
	}
	@RequestMapping("/KBI/Edit/Submit")
	public String editKBISubmit (IndikatorKBI indikator) {
		
		//get kbi by id
		//model isinya listof indikatorkbi
		System.out.println("====================MASUK SINI EDIT SUBMIT==================");
		System.out.println(indikator);
		
		
		kbiDAO.editIndikatorKBI(indikator);
		kbiDAO.addLog(getNIK(), "Edit Indikator KBI");
		System.out.println("====================KELUAR DARI EDIT SUBMIT==================");
		return "redirect:/KBI/Edit/Selection/"+indikator.getId_kbi();
	}
	
	@RequestMapping("/KBI/Delete/Submit/{id}")
	public String deleteKBIIndikator (Principal principal, @PathVariable(value = "id") String id, HttpSession session) {
		
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("HC HO") && !role.equalsIgnoreCase("HC Unit")) {
			return "redirect:/login"; 
		}
		System.out.println("====================MASUK SINI DELETE SUBMIT==================");
		IndikatorKBI indikator= kbiDAO.getIndikatorKBI(id);
		String id_kbi = indikator.getId_kbi();
		kbiDAO.deleteIndikatorKBI(id);
		kbiDAO.addLog(getNIK(), "Delete Indikator KBI");
		System.out.println("====================KELUAR DARI DELETE SUBMIT==================");
		
		return "redirect:/KBI/Edit/Selection/"+id_kbi;
	}
	
	
	@RequestMapping("/KBI/Evaluate/Selection")
	public String nilaiBawahanSelection (Principal principal, Model model, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		// manajer 
		if (!role.equalsIgnoreCase("Head Division") && !role.equalsIgnoreCase("BOD") && !role.equalsIgnoreCase("Head Unit")) {
			return "redirect:/login"; 
		}
		//getkaryawan atau bawahan
		List<Karyawan> bawahan = kbiDAO.getBawahanForManager(nik);
		//menghapus nik sendiri
		for (int i = 0; i < bawahan.size(); i ++) {
			if (bawahan.get(i).getNik().equals(nik)) {
				bawahan.remove(bawahan.get(i));
			}
		}
		System.out.println(bawahan);
		System.out.println(bawahan.size());
		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		List<KBI_Karyawan> listKBIKaryawan = new ArrayList<KBI_Karyawan>();
		for (int i = 0; i < bawahan.size(); i++) {
			System.out.println("================ITERASI KE " + i + " ===================");
			KBI_Karyawan mgr = kbiDAO.getKBIKaryawan(kbiByPeriode.get(0).getId(), bawahan.get(i).getNik());
			KBI_Karyawan non_mgr = kbiDAO.getKBIKaryawan(kbiByPeriode.get(1).getId(), bawahan.get(i).getNik());
			System.out.println("===============SYSO MGR================");
			System.out.println(mgr);
			System.out.println("===============SYSO NON-MGR================");
			System.out.println(non_mgr);
			if (mgr != null) {
				System.out.println("if");
				String karyawan_approver = mgr.getKaryawan_approver_nilai();
				String hc_approver = mgr.getHc_approver_nilai();
				
				//add di penilaian kalo ada yg ga null dan 
				//direject
				System.out.println(mgr);
				if (karyawan_approver != null && karyawan_approver.equals("REJECTED")) {
					mgr.setStatus("Rejected by Approver");
					
				} else if (hc_approver != null && hc_approver.equals("REJECTED")) {
					mgr.setStatus("Rejected by Approver");
				}
				List<JawabanKBI> jawaban = kbiDAO.getJawabanKBI(mgr.getId_kbi(), mgr.getNik_pegawai());
				mgr.setJawaban(jawaban);
				listKBIKaryawan.add(mgr);	
				
			} else if (non_mgr != null) {
				System.out.println("else if");
				String karyawan_approver = non_mgr.getKaryawan_approver_nilai();
				String hc_approver = non_mgr.getHc_approver_nilai();
				
				//remove di penilaian kalo 22nya ga null dan 
				//22nya ga reject
				if (karyawan_approver != null && karyawan_approver.equals("REJECTED")) {
					non_mgr.setStatus("Rejected By Approver");
				} else if (hc_approver != null && hc_approver.equals("REJECTED")) {
					non_mgr.setStatus("Rejected By Approver");
				}
				List<JawabanKBI> jawaban = kbiDAO.getJawabanKBI(non_mgr.getId_kbi(), non_mgr.getNik_pegawai());
				non_mgr.setJawaban(jawaban);
				listKBIKaryawan.add(non_mgr);	

			} else {
				System.out.println("else");
				KBI_Karyawan KBIKaryawanBaru = new KBI_Karyawan();
				KBIKaryawanBaru.setNik_pegawai(bawahan.get(i).getNik());
				KBIKaryawanBaru.setNama_pegawai(bawahan.get(i).getName());
				KBIKaryawanBaru.setStatus("Waiting for Grading");
				listKBIKaryawan.add(KBIKaryawanBaru);
			} 
			
		}
		//ambil jawaban
			System.out.println(listKBIKaryawan);
			System.out.println(listKBIKaryawan.size());
			for (int j = 0; j < listKBIKaryawan.size(); j++) {
				List<JawabanKBI> jawaban = listKBIKaryawan.get(j).getJawaban();
				if (jawaban != null ) {
					for (int i = 0; i < jawaban.size(); i++) {
						JawabanKBI jawabanNow = jawaban.get(i);
						String id_indikator = jawabanNow.getId_indikator_kbi();
						IndikatorKBI indikatorNow = kbiDAO.getIndikatorKBI(id_indikator);
						String judul = indikatorNow.getJudul();
						String deskripsi = indikatorNow.getDeskripsi();
						listKBIKaryawan.get(j).getJawaban().get(i).setJudul(judul);
						listKBIKaryawan.get(j).getJawaban().get(i).setDeskripsi(deskripsi);
					}
				}
			}
		System.out.println(listKBIKaryawan);
		//model isinya listof kbikaryawan
		model.addAttribute("karyawan", listKBIKaryawan);
		return "KBI-StaffEvaluationSelection";
	}
	
	@RequestMapping("/KBI/Evaluate/Selection/{id}")
	public String nilaiBawahan (Principal principal, Model model, @PathVariable(value = "id") String id, HttpSession session) {	
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		// manajer 
		if (!role.equalsIgnoreCase("Head Division") && !role.equalsIgnoreCase("BOD") && !role.equalsIgnoreCase("Head Unit")) {
			return "redirect:/login"; 
		}
		Karyawan karyawan = kbiDAO.getKaryawanByNIK(id);
		int sto_id = karyawan.getSto_id();
		String divisi = kbiDAO.getDivisi(sto_id);
		String unit = kbiDAO.getUnit(sto_id);
		karyawan.setDivisi(divisi);
		karyawan.setUnit(unit);
		//cari id_kbi sesuai periode penilaian
		
		String id_kbi = "";

		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
		boolean staf = isStaf(id);
		System.out.println("=============IS STAF EVALUATE SELECTION"+staf+"==============");
		System.out.println("hello");
		//ambil id_kbi if mgr or non mgr
		if (staf == false) {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		} else {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Non-Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		}
		
		
		//get kbi by id
		//model isinya listof indikatorkbi
		KBI kbi = kbiDAO.getKBIById(id_kbi);
		System.out.println(kbi);
		List<IndikatorKBI> indikator = kbi.getIndikator();
		System.out.println(indikator);
		String jabatan = kbi.getJabatan();
		//model isinya info ttg karyawan
		//model isinya indikator kbi
		model.addAttribute("jabatan", jabatan);
		model.addAttribute("indikator", indikator);
		model.addAttribute("karyawan", karyawan);
		model.addAttribute("nik", id);
		return "KBI-StaffEvaluation";

	}
	
	@RequestMapping(value="/KBI/Evaluate/Selection/Submit", method = RequestMethod.GET)
	public String nilaiBawahanSubmit (HttpServletRequest request, HttpServletResponse response) throws IOException  {
		String nik_pegawai = request.getParameter("nik");
		System.out.println("===================SYSO NIK=====================");
		System.out.println(nik_pegawai);
		
		String[] approval_list = request.getParameter("approval_list").split(",");
		System.out.println(approval_list);
		System.out.println("===================MASUK SUBMIT=====================");
		//cari id_kbi sesuai periode penilaian
		
		String id_kbi = "";

		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
		boolean staf = isStaf(nik_pegawai);
		System.out.println("=============IS STAF "+staf+"==============");
		//ambil id_kbi if mgr or non mgr
		if (staf == false) {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		} else {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Non-Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		}
		System.out.println("ini id_kbi"+id_kbi);

		boolean karyawanUpdate = true;
		KBI_Karyawan adaKaryawan = kbiDAO.getKBIKaryawan(id_kbi, nik_pegawai);
		if (adaKaryawan == null) {
			karyawanUpdate = false;
		}
		System.out.println("=============ADA KARYAWAN"+karyawanUpdate+"==============");
		//nik penilai, yg lg login 
		String nik_penilai = getNIK();
		List<JawabanKBI> listJawaban = new ArrayList<JawabanKBI>();
		float nilai_akhir = 0;
		//perhitungan nilai akhir
		for (int i = 0; i < approval_list.length; i++) {
			System.out.println(approval_list[i]);
			String[] parts = approval_list[i].split(":");
			String id_indikator = parts[0]; // 004
			String nilai = parts[1]; // 034556
			System.out.println(id_indikator);
			System.out.println(nilai);
			IndikatorKBI kbi = kbiDAO.getIndikatorKBI(id_indikator);
			int nilaiJawaban = Integer.parseInt(nilai);
			double bobotJawaban = kbi.getBobot();
			double hasil = (bobotJawaban/100) * nilaiJawaban;
			System.out.println(bobotJawaban/100);
			JawabanKBI jawaban = new JawabanKBI();
			jawaban.setId_kbi(id_kbi);
			jawaban.setId_indikator_kbi(id_indikator);
			jawaban.setBobot(bobotJawaban);
			jawaban.setJawaban(nilaiJawaban);
			jawaban.setNik_pegawai(nik_pegawai);
			jawaban.setNik_penilai(nik_penilai);
			jawaban.setHasil(hasil);
			if (karyawanUpdate == true) {
				kbiDAO.editJawabanKBI(jawaban);
				kbiDAO.addLog(getNIK(), "Edit Jawaban KBI");
			} else {
				kbiDAO.makeJawabanKBI(jawaban);
				kbiDAO.addLog(getNIK(), "Make Jawaban KBI");
			}
		
			listJawaban.add(jawaban);
			nilai_akhir += hasil;
		}
		//nilai_akhir memiliki skala 100
		nilai_akhir = nilai_akhir * 10;
		System.out.println(nilai_akhir);
	//	String id_indikator = listJawaban.get(0).getId_indikator_kbi();
	//	String id_kbi = kbiDAO.getID_KBI(id_indikator);
	
		Karyawan karyawanBaru = kbiDAO.getKaryawanByNIK(nik_pegawai);
		int sto_id = karyawanBaru.getSto_id();
		String divisi = kbiDAO.getDivisi(sto_id);
		String unit = kbiDAO.getUnit(sto_id);
		karyawanBaru.setDivisi(divisi);
		karyawanBaru.setUnit(unit);
		System.out.println(karyawanBaru);
		KBI_Karyawan karyawan = new KBI_Karyawan();
		karyawan.setNama_pegawai(karyawanBaru.getName());
		karyawan.setDivisi(divisi);
		karyawan.setUnit(unit);
		karyawan.setId_kbi(id_kbi);
		karyawan.setJawaban(listJawaban);
		karyawan.setNik_pegawai(nik_pegawai);
		karyawan.setNik_penilai(nik_penilai);
		karyawan.setNilai_akhir(nilai_akhir);
		System.out.println(karyawan);
		
		if (karyawanUpdate == true) {
			kbiDAO.editKBIKaryawan(karyawan);
		} else {
			kbiDAO.makeKBIKaryawan(karyawan);
		}
	
		return "redirect:/KBI/Evaluate/Selection";
	}
	
	
	@RequestMapping("/KBI/HC/Result")
	public String approvalHC (Principal principal, Model model, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("HC HO") && !role.equalsIgnoreCase("HC Unit")) {
			return "redirect:/login"; 
		}
		List<KBI_Karyawan> listKaryawan = new ArrayList<KBI_Karyawan>();
		List<KBI_Karyawan> listKaryawanFix = new ArrayList<KBI_Karyawan>();
		boolean approvalDone = true;
		
		//getkaryawan atau bawahan
		List<Karyawan> karyawan = kbiDAO.getKaryawanForHC(nik);
		//menghapus nik sendiri
		for (int i = 0; i < karyawan.size(); i ++) {
			if (karyawan.get(i).getNik().equals(nik)) {
				karyawan.remove(karyawan.get(i));
			}
		}
		//dapet list of nik
		System.out.println(karyawan);
		
		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
	    
		//get kbi karyawan by nik yg ada
		for (int i = 0; i < karyawan.size(); i++) {
			KBI_Karyawan karyawanNow = kbiDAO.getKBIKaryawanByNIK(karyawan.get(i).getNik());
			if (karyawanNow != null) {
				listKaryawan.add(karyawanNow);
			}
		}
		System.out.println(listKaryawan);
		//ambil semua kbikaryawan yg idnya sesuai di periode
		for (int i = 0; i < listKaryawan.size(); i++) {
			for (int j = 0; j < kbiByPeriode.size(); j++) {
				System.out.println(kbiByPeriode.get(j).getId());
				if (listKaryawan.get(i).getId_kbi().equals(kbiByPeriode.get(j).getId())) {
					List<JawabanKBI> jawaban_karyawan = kbiDAO.getJawabanKBI(kbiByPeriode.get(j).getId(), listKaryawan.get(i).getNik_pegawai());
					System.out.println(jawaban_karyawan);
					listKaryawan.get(i).setJawaban(jawaban_karyawan);
					//ganti status
					String status = "Approved";
					if (listKaryawan.get(i).getHc_approver_nilai() == null) {
						status = "Waiting";
						approvalDone = false;
					} else if (listKaryawan.get(i).getHc_approver_nilai().equals("REJECTED")) {
						status = "Rejected";
					}
					listKaryawan.get(i).setHc_approver_nilai(status);
					listKaryawanFix.add(listKaryawan.get(i));
					System.out.println("ADDED");
				}
			}
		}
		//ambil jawaban
		System.out.println(listKaryawanFix);
		System.out.println(listKaryawanFix.size());
		for (int j = 0; j < listKaryawanFix.size(); j++) {
			List<JawabanKBI> jawaban = listKaryawanFix.get(j).getJawaban();
			for (int i = 0; i < jawaban.size(); i++) {
				JawabanKBI jawabanNow = jawaban.get(i);
				String id_indikator = jawabanNow.getId_indikator_kbi();
				IndikatorKBI indikatorNow = kbiDAO.getIndikatorKBI(id_indikator);
				String judul = indikatorNow.getJudul();
				String deskripsi = indikatorNow.getDeskripsi();
				listKaryawanFix.get(j).getJawaban().get(i).setJudul(judul);
				listKaryawanFix.get(j).getJawaban().get(i).setDeskripsi(deskripsi);
			}
		}
		System.out.println(listKaryawanFix);
		
		model.addAttribute("kbi_karyawan", listKaryawanFix);
		model.addAttribute("approvalDone", approvalDone);		
//		System.out.println(kbi_karyawan);
		return "KBI-HCResult";
	}
	
	
	@RequestMapping(value = "/KBI/HC/Result/Submit", method = RequestMethod.GET)
	public String approvalHCResult (HttpServletRequest request, HttpServletResponse response) throws IOException  {
		
		String hc_approver_nilai = getNIK();
		String[] approval_list = request.getParameter("approval_list").split(",");
		System.out.println(approval_list);
		System.out.println("===================MASUK SUBMIT=====================");
		for (int i = 0; i < approval_list.length; i++) {
			System.out.println(approval_list[i]);
			String[] parts = approval_list[i].split(":");
			String id_karyawan = parts[0]; // 004
			String approval = parts[1]; // 034556
			System.out.println(id_karyawan);
			System.out.println(approval);
			KBI_Karyawan karyawan = kbiDAO.getKBIKaryawanByID(id_karyawan);
			if (approval.equals("true")) {
				kbiDAO.approveByHC(karyawan.getId_kbi(), karyawan.getNik_pegawai(), hc_approver_nilai);
				kbiDAO.addLog(getNIK(), "Approve By HC KBI Karyawan"+id_karyawan);
			} else {
				kbiDAO.approveByHC(karyawan.getId_kbi(), karyawan.getNik_pegawai(), "REJECTED");
				kbiDAO.addLog(getNIK(), "Reject By HC KBI Karyawan"+id_karyawan);
			}
		}
		
		return "KBI-HCResult";	
	}

	
	@RequestMapping("/KBI/Karyawan/Result")
	public String approvalKaryawan (Principal principal, Model model, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("Head Division") && !role.equalsIgnoreCase("BOD") && !role.equalsIgnoreCase("Head Unit") && !role.equalsIgnoreCase("User")) {
			return "redirect:/login"; 
		}
		//SEMUA INDIKATOR HARUS SUDAH TERJAWAB
		boolean approvalDone = true;
		//getkbi karyawan by nik and id_kbi
		String id_kbi = "";

		//id_periode_penilaian = 11;
		String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
		System.out.println(id_periode_penilaian);
		//get list of kbi by periode
		List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
		System.out.println(kbiByPeriode);
		boolean staf = isStaf(nik);
		boolean manager = !staf;
		//ambil id_kbi if mgr or non mgr
		if (manager == true) {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		} else {
			for (int i = 0; i < kbiByPeriode.size(); i++) {
				if (kbiByPeriode.get(i).getJabatan().equals("Non-Managerial")) {
					id_kbi = kbiByPeriode.get(i).getId();
				}
			}
		}
		System.out.println("ini id_kbi"+id_kbi);
		KBI_Karyawan karyawan = kbiDAO.getKBIKaryawan(id_kbi, nik);
		System.out.println(karyawan);
		if (karyawan != null) {
			List<JawabanKBI> jawaban_karyawan = kbiDAO.getJawabanKBI(id_kbi, nik);
			System.out.println(jawaban_karyawan);
			karyawan.setJawaban(jawaban_karyawan);
			System.out.println("INI KARYAWAN"+karyawan);

			KBI kbi = kbiDAO.getKBIById(id_kbi);
			System.out.println(kbi);
			List<IndikatorKBI> indikator = kbi.getIndikator();
			System.out.println(indikator);
			String jabatan = kbi.getJabatan();
			
			List<JawabanKBI> jawaban = karyawan.getJawaban();
			System.out.println("JAWABAN SIZE"+jawaban.size());
			if (jawaban.size() != 0) {
				for (int i = 0; i < jawaban.size(); i++) {
					JawabanKBI jawabanNow = jawaban.get(i);
					String id_indikator = jawabanNow.getId_indikator_kbi();
					IndikatorKBI indikatorNow = kbiDAO.getIndikatorKBI(id_indikator);
					String judul = indikatorNow.getJudul();
					String deskripsi = indikatorNow.getDeskripsi();
					jawaban.get(i).setJudul(judul);
					jawaban.get(i).setDeskripsi(deskripsi);
				}
			} else {
				jawaban = new ArrayList<JawabanKBI>();
				System.out.println(indikator);
				for (int i = 0; i < indikator.size(); i++) {
					JawabanKBI jawabanBaru = new JawabanKBI();
					IndikatorKBI indikatorNow = indikator.get(i);
					jawabanBaru.setJudul(indikatorNow.getJudul());
					jawabanBaru.setDeskripsi(indikatorNow.getDeskripsi());
					jawabanBaru.setBobot(indikatorNow.getBobot());
					jawabanBaru.setJawaban(0);
					jawabanBaru.setHasil(0);
					jawaban.add(jawabanBaru);
				}
			}
			
			String status = "Approved";
			if (karyawan.getKaryawan_approver_nilai() == null) {
				status = "Waiting for Approval";
				approvalDone = false;
			} else if (karyawan.getKaryawan_approver_nilai().equals("REJECTED")) {
				status = "Rejected";
			}
			model.addAttribute("jabatan", jabatan);
			System.out.println(jabatan);
			model.addAttribute("jawaban", jawaban);
			System.out.println(jawaban);
			model.addAttribute("karyawan", karyawan);
			System.out.println(karyawan);
			model.addAttribute("status", status);
			model.addAttribute("approvalDone", approvalDone);
		}
		
		return "KBI-MyResult";
	}
	
	@RequestMapping("/KBI/Karyawan/Result/Submit/Approve")
	public String approvalKaryawanApprove (Principal principal, Model model, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("Head Division") && !role.equalsIgnoreCase("BOD") && !role.equalsIgnoreCase("Head Unit") && !role.equalsIgnoreCase("User")) {
			return "redirect:/login"; 
		}
		//getkbi karyawan by nik and id_kbi
				String id_kbi = "";

				//id_periode_penilaian = 11;
				String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
				System.out.println(id_periode_penilaian);
				//get list of kbi by periode
				List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
				System.out.println(kbiByPeriode);
				boolean staf = isStaf(nik);
				boolean manager = !staf;
				//ambil id_kbi if mgr or non mgr
				if (manager == true) {
					for (int i = 0; i < kbiByPeriode.size(); i++) {
						if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
							id_kbi = kbiByPeriode.get(i).getId();
						}
					}
				} else {
					for (int i = 0; i < kbiByPeriode.size(); i++) {
						if (kbiByPeriode.get(i).getJabatan().equals("Non-Managerial")) {
							id_kbi = kbiByPeriode.get(i).getId();
						}
					}
				}
		kbiDAO.approveByKaryawan(id_kbi, nik, nik);
		kbiDAO.addLog(getNIK(), "Approve By Karyawan");
		return "redirect:/KBI/Karyawan/Result";
	}
	
	@RequestMapping("/KBI/Karyawan/Result/Submit/Reject")
	public String approvalKaryawanReject (Principal principal, Model model, HttpSession session) {
		System.out.println("============MASUK REJECTED=========");
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (!role.equalsIgnoreCase("Head Division") && !role.equalsIgnoreCase("BOD") && !role.equalsIgnoreCase("Head Unit") && !role.equalsIgnoreCase("User")) {
			return "redirect:/login"; 
		}
		//getkbi karyawan by nik and id_kbi
				String id_kbi = "";

				//id_periode_penilaian = 11;
				String id_periode_penilaian = kbiDAO.getPeriodePenilaianAktif();
				System.out.println(id_periode_penilaian);
				//get list of kbi by periode
				List<KBI> kbiByPeriode = kbiDAO.getKBIByPeriode(id_periode_penilaian);
				System.out.println(kbiByPeriode);
				boolean staf = isStaf(nik);
				boolean manager = !staf;
				//ambil id_kbi if mgr or non mgr
				if (manager == true) {
					for (int i = 0; i < kbiByPeriode.size(); i++) {
						if (kbiByPeriode.get(i).getJabatan().equals("Managerial")) {
							id_kbi = kbiByPeriode.get(i).getId();
						}
					}
				} else {
					for (int i = 0; i < kbiByPeriode.size(); i++) {
						if (kbiByPeriode.get(i).getJabatan().equals("Non-Managerial")) {
							id_kbi = kbiByPeriode.get(i).getId();
						}
					}
				}
		System.out.println(id_kbi);
		System.out.println(nik);
		kbiDAO.approveByKaryawan(id_kbi, nik, "REJECTED");
		kbiDAO.addLog(getNIK(), "Reject By Karyawan");
		return "redirect:/KBI/Karyawan/Result";
	}

}
