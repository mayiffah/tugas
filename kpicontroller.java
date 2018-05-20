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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.b04.model.KPI;
import com.example.b04.model.KPI_Detail;
import com.example.b04.model.KPI_Karyawan;
import com.example.b04.model.Karyawan;
import com.example.b04.service.KPIService;


@Controller
public class KPIController {

	@Autowired
    KPIService kpiDAO;
	
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
	
	public List<String> listBawahan () {
		String nik = getNIK();
		List<String> bawahan = kpiDAO.searchBawahan(nik);
		for (int i = 0; i < bawahan.size(); i ++) {
			if (bawahan.get(i).equals(nik)) {
				bawahan.remove(bawahan.get(i));
			}
		}
		
		return bawahan;
	}
	
	public List<String> listKaryawan () {
		String nik = getNIK();
		List<String> karyawan = kpiDAO.searchKaryawan(nik);
		for (int i = 0; i < karyawan.size(); i ++) {
			if (karyawan.get(i).equals(nik)) {
				karyawan.remove(karyawan.get(i));
			}
		}
		return karyawan;
	}
	
	public List<KPI> sesuaiPeriodeKPI() {
		
		//asumsi periode penilaian yg aktif = tahun 2018, periode 1
		String periodenow = kpiDAO.periodePenilaianAktif();
		System.out.println(periodenow);
		List<KPI> semuakpi = kpiDAO.getAllKPI();
		System.out.println(semuakpi);
		
		//mencari kpi sesuai periode
		List<KPI> sesuaiPeriode = new ArrayList<KPI>();
		for (int i = 0; i < semuakpi.size(); i++) {
			System.out.println("periodepenilaian:" + semuakpi.get(i).getId_periode_penilaian());
			if (semuakpi.get(i).getId_periode_penilaian().equals(periodenow)) {
				sesuaiPeriode.add(semuakpi.get(i));
				System.out.println("added");
			}
		}
		System.out.println(sesuaiPeriode);
		
		return sesuaiPeriode;
	}
	
	public List<KPI> kpiDilihat(List<KPI> sesuaiPeriode, List<String> karyawanFix) {

		String namaPegawai = "";
		List<KPI> kpiDilihat = new ArrayList<KPI>();
		
		for (int i = 0; i < sesuaiPeriode.size(); i++) {
			for (int j = 0; j <karyawanFix.size(); j++) {
				if (sesuaiPeriode.get(i).getId_manajer().equals(karyawanFix.get(j))) {
					System.out.println("periodepenilaian:" + sesuaiPeriode.get(i).getId_periode_penilaian());
						namaPegawai = kpiDAO.searchPegawai(sesuaiPeriode.get(i).getId_manajer());
						System.out.println(namaPegawai);
					    sesuaiPeriode.get(i).setNama_manajer(namaPegawai);
						kpiDilihat.add(sesuaiPeriode.get(i));
						System.out.println("added");
				}
			}
		}
		return kpiDilihat;
	}
	
	public List<KPI_Karyawan> kpiKaryawanDilihat(List<KPI> sesuaiPeriode, List<String> karyawanFix) {
		List<KPI_Karyawan> listKPIKaryawan = new ArrayList<KPI_Karyawan>();
		for (int i = 0; i < sesuaiPeriode.size(); i++) {
			for (int j = 0; j <karyawanFix.size(); j++) {
				KPI_Karyawan karyawan = kpiDAO.getKPIKaryawan(sesuaiPeriode.get(i).getId(), karyawanFix.get(j));
				if (karyawan != null) {
					listKPIKaryawan.add(karyawan);
				}
			}
		}
		
		
		return listKPIKaryawan;
	}
	
	public float hitungNilaiAkhir(float nilai, float bobot) {
	
		float bobotAsli;
		float nilaiSementara;
		System.out.println("bobot masuk"+bobot);
		
		System.out.println("nilai masuk"+nilai);
		bobotAsli = bobot/100;
		nilaiSementara = bobotAsli * nilai;
		System.out.println(nilaiSementara);
		
		
		return nilaiSementara;
	}
	
	public boolean isStaf(String nik) {
		System.out.println("masuk SINIII");
		List<String> stoStaf = kpiDAO.selectStaf();
		String sto = kpiDAO.selectSto(nik);
		System.out.println(sto);
		
		for (int i = 0; i < stoStaf.size(); i++) {
			if (sto.equals(stoStaf.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	@RequestMapping("/KPI/Personal")
	public String viewMyKPIUpdated(Model model, Principal principal, HttpSession session) {
		/**
		 *
		 * liatKPI
		 		mgr dan karyawan
		 * */
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		String periodenow = kpiDAO.periodePenilaianAktif();
		System.out.println("periodenow"+periodenow);
		List<KPI> kpi = new ArrayList<KPI>();
		String idKPI = "0";
		// manajer 
		if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit")) {
			kpi = kpiDAO.getKPI(nik);
		// karyawan, cari nik manajernya dulu
		} else if (role.equalsIgnoreCase("User")) {
			String id_atasan = kpiDAO.searchAtasan(nik);
			String nikAtasan = kpiDAO.getNIK(id_atasan);
			kpi = kpiDAO.getKPI(nikAtasan);
		} else {
			return "redirect:/login"; 
		}
		
		//cari kpi berdasarkan nik
		System.out.println(kpi);
		for (int i = 0; i < kpi.size(); i++) {
			System.out.println(kpi.get(i).getId_manajer() + " per:"+ kpi.get(i).getId_periode_penilaian() + " id:" +kpi.get(i).getId());
		}
		
		//dlm satu periode hanya memiliki 1 kpi
		//mencari kpi yg aktif
		for (int i = 0; i < kpi.size(); i++) {
			System.out.println(kpi.get(i).getId_periode_penilaian());
			if (kpi.get(i).getId_periode_penilaian().equals(periodenow)) {
				idKPI = kpi.get(i).getId();
				System.out.println("kpi:"+idKPI);
			}
		}
		if (idKPI.equals("0")) {
			model.addAttribute("noKPI", true);
			return "KPI-ViewMyKPI";
		}
		List<KPI_Detail> kpi_komponen = kpiDAO.getKPIDetails(idKPI);
		System.out.println(kpi_komponen.size());
		System.out.println(kpi_komponen);
		
		Karyawan karyawan = kpiDAO.getKaryawanByNIK(nik);
		String divisi = kpiDAO.getDivisi(karyawan.getSto_id());
		String unit = kpiDAO.getUnit(karyawan.getSto_id());
		karyawan.setDivisi(divisi);
		karyawan.setUnit(unit);
		boolean isManager = true;
		boolean isGraded = true;
		boolean isApprovedByMe = true;
		boolean kpiApproved = true;
		String nilai_akhir = "";
		String status = "";
		if(isStaf(nik) == true) {
			isManager = false;
			KPI_Karyawan kpi_karyawan = kpiDAO.getKPIKaryawan(idKPI, nik);
			if (kpi_karyawan == null) {
				isGraded = false;
				isApprovedByMe = false;
			} else {
				nilai_akhir = kpi_karyawan.getNilai();
				status = kpi_karyawan.getStaf_approver_nilai();
				if (status == null) {
					status = "Waiting";
					isApprovedByMe = false;
				} else if (status.equals("REJECTED")) {
					status = "Rejected";
				} else {
					status = "Approved";
				}
			}
		} else {
			KPI kpi_manager = kpiDAO.getKPIbyId(idKPI);
			String mgr_approver = kpi_manager.getMgr_approver();
			String hc_approver = kpi_manager.getHc_approver();
			if (mgr_approver == null || hc_approver == null || mgr_approver.equals("REJECTED") || hc_approver.equals("REJECTED")) {
				kpiApproved = false;
			}
			if (kpi_manager.getNilai() == 0) {
				isGraded = false;
				isApprovedByMe = false;
			} else {
				nilai_akhir = Float.toString(kpi_manager.getNilai());
				status = kpi_manager.getKaryawan_approver_nilai();
				if (status == null) {
					status = "Waiting";
					isApprovedByMe = false;
				} else if (status.equals("REJECTED")) {
					status = "Rejected";
				} else {
					status = "Approved";
				}
			}
		}
		
		model.addAttribute("id_kpi", idKPI);
		model.addAttribute("karyawan", karyawan);
		model.addAttribute("isGraded", isGraded);
		model.addAttribute("isManager", isManager);
		model.addAttribute("isApproved", isApprovedByMe);
		System.out.println(isApprovedByMe);
		model.addAttribute("kpiApproved", kpiApproved);
		model.addAttribute("status", status);
		model.addAttribute("nilai_akhir", nilai_akhir);
		model.addAttribute("model", kpi_komponen);
		return "KPI-ViewMyKPI";
	}
	
	@RequestMapping("/KPI/Edit/Add/Submit")
	public String editKBIAddSubmit (
			@RequestParam(value = "id_kpi", required = true) String id_kpi,
	        @RequestParam(value = "judul", required = true) String judul,
	        @RequestParam(value = "deskripsi", required = true) String deskripsi,
	        @RequestParam(value = "target", required = true) String target,
	        @RequestParam(value = "bobot", required = true) String bobot) {
		KPI_Detail kpi_detail = new KPI_Detail();
		kpi_detail.setId_kpi(Integer.parseInt(id_kpi));
		kpi_detail.setJudul(judul);
		kpi_detail.setDeskripsi(deskripsi);
		kpi_detail.setNilai_target(Float.parseFloat(target));
		kpi_detail.setBobot(Float.parseFloat(bobot));
		kpiDAO.makeKPI_Detail(kpi_detail);
		kpiDAO.addLog(getNIK(), "Create Indikator KPI");
		return "redirect:/KPI/Personal"; 
		
	}
	
	@RequestMapping("/KPI/Draft/Create")
	public String makeKPI(Principal principal, HttpSession session, Model model) {
		/**
		 *
		 * makeKPI role: manajer-- KPI perancangan

		 * */
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		String periode_penilaian = kpiDAO.periodePenilaianAktif();
		// manajer 
		if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit")) {
			List<KPI> kpi = kpiDAO.getKPI(nik);
			for (int i = 0; i < kpi.size(); i++) {
				if (kpi.get(i).getId_periode_penilaian().equals(periode_penilaian)) {
					model.addAttribute("done", true);
				}
			}
			System.out.println(kpi);
			return "KPI-DraftForm";
		} else {
			return "redirect:/login"; 
		}
	} 
	
	
	@RequestMapping("/KPI/Draft/Create/Submit")
	public String makeKPISubmit (
			Model model,
			@RequestParam(value = "judul", required = true) List<String> judul,
	        @RequestParam(value = "deskripsi", required = true) List<String> deskripsi,
	        @RequestParam(value = "target", required = true) List<String> target,
	        @RequestParam(value = "bobot", required = true) List<String> bobot)
	{
		
		//search nik
		String id_manajer = getNIK();
		
		//search periode penilaian, cari idnya
		String id_periode_penilaian = kpiDAO.periodePenilaianAktif();
		
		KPI kpi = new KPI ();
		kpi.setId_manajer(id_manajer);
		kpi.setId_periode_penilaian(id_periode_penilaian);
		System.out.println(kpi);

		kpiDAO.makeKPI(kpi);
		kpiDAO.addLog(id_manajer, "Buat KPI");
		//bikin page 
		KPI kpiBaru = kpiDAO.selectKPIByNewest();
		String id_kpi_string = kpiBaru.getId();
		int id_kpi = Integer.parseInt(id_kpi_string);
		makeKPI_Detail(id_kpi, judul, deskripsi, target, bobot); 
		model.addAttribute("message", "KPI sukses dibuat");
		return "KPI-DraftForm";
	}
	
	 
	public void makeKPI_Detail (int id_kpi, List<String> judul,  List<String> deskripsi, List<String> target,  List<String> bobot) {
		 
		   List<String> jumlahJudul = judul;
		   System.out.println(jumlahJudul);
		   List<String> jumlahDeskripsi = deskripsi;
		   List<String> jumlahTarget = target;
		   System.out.println(jumlahTarget);
		   System.out.println(jumlahDeskripsi);
		   List<String> jumlahBobot = bobot;
		   System.out.println(jumlahBobot);
		   
		   int jumlahKomponen = jumlahJudul.size()-1;
		   
		   for (int i = 0; i < jumlahKomponen; i++) {
			String judulNow = jumlahJudul.get(i);
			String deskripsiNow = jumlahDeskripsi.get(i);
			String targetNow = jumlahTarget.get(i);
	 		String bobotNow = jumlahBobot.get(i);
	 		float targetNowFloat = Float.parseFloat(targetNow);
	 		float bobotNowFloat = Float.parseFloat(bobotNow);
			
			KPI_Detail kpi_detail = new KPI_Detail (0, id_kpi, judulNow, deskripsiNow, 0, 0, targetNowFloat, 0, bobotNowFloat);
		 	kpiDAO.makeKPI_Detail(kpi_detail);
		 	kpiDAO.addLog(getNIK(), "Buat Indikator KPI");
		   }
		   
	} 


	@RequestMapping("/KPI/Draft/View/Selection")
	public String selectKPIApproval(Model model, Principal principal, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		List<String> karyawanFix = new ArrayList<String>();
		boolean manajerStaf = false;
		List<KPI> sesuaiPeriode = new ArrayList<KPI>();
		List<KPI> kpiDilihat = new ArrayList<KPI>();
		
		// manajer staf dan nonstaf
		if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit"))
		{
			List<String> bawahan = listBawahan();
			if (!bawahan.isEmpty()) {
				System.out.println(bawahan);
				for (int i = 0; i < bawahan.size(); i++) {
					if (isStaf(bawahan.get(i)) == true) {
						manajerStaf = true;
						break;
					}
				}
				karyawanFix = bawahan;
			}
			if (manajerStaf == true) {
				model.addAttribute("manajerStaf", true);
				return "KPI-DraftApprovalSelection"; 
			}

			sesuaiPeriode = sesuaiPeriodeKPI();
			kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
			System.out.println(kpiDilihat.size());
			System.out.println(kpiDilihat);
			
			//setting status
			for (int i = 0; i < kpiDilihat.size(); i++) {
				KPI kpiStatus = kpiDAO.getKPIbyId(kpiDilihat.get(i).getId());
				String status = "";
				String mgr_approver = kpiStatus.getMgr_approver();
				if (mgr_approver != null && mgr_approver.equals("REJECTED")) {
					status = "Rejected";
				} else if (mgr_approver == null) {
					status = "Waiting for Approval";
				} else {
					status = "Approved";
				}
				//mengganti status
				kpiDilihat.get(i).setStatus(status);
				System.out.println("====================STATUS TELAH DISET DI CONTROLLER================");
				System.out.println(kpiDilihat.get(i).getStatus());
				
			}
			
		// HC	
		} else if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
			List<String> karyawan = listKaryawan();
			if (!karyawan.isEmpty()) {
				System.out.println(karyawan);
				karyawanFix = karyawan;
			}

			sesuaiPeriode = sesuaiPeriodeKPI();
			kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
			System.out.println(kpiDilihat.size());
			System.out.println(kpiDilihat);
			
			//setting status
			for (int i = 0; i < kpiDilihat.size(); i++) {
				KPI kpiStatus = kpiDAO.getKPIbyId(kpiDilihat.get(i).getId());
				String status = "";
				String hc_approver = kpiStatus.getHc_approver();
				if (hc_approver != null && hc_approver.equals("REJECTED")) {
					status = "Rejected";
				} else if (hc_approver == null) {
					status = "Waiting for Approval";
				} else {
					status = "Approved";
				}
				//mengganti status
				kpiDilihat.get(i).setStatus(status);
				System.out.println("====================STATUS TELAH DISET DI CONTROLLER================");
				System.out.println(kpiDilihat.get(i).getStatus());
				
			}
		} else {
			return "redirect:/login"; 
		}
		model.addAttribute("model", kpiDilihat);
		
		return "KPI-DraftApprovalSelection";
	}
	
	@RequestMapping("/KPI/Draft/View/{id}")
	public String KPIApproval(Model model, Principal principal, HttpSession session, @PathVariable(value = "id") String id) {
		/**
		 *
		 * 	approveKPIByManajer non Staf role: Manajer non Staf -- hasilRancangan
			approveKPIByHC role: HC -- hasilRancangan


		 * */
		
	//kalo manajer staf, ambil id dari kpi dia sendiri
			String roleValid = isRoleValid(principal);
			System.out.println(roleValid);
			String nik = getNIK();
			String role = getRole(session);
			System.out.println("ini nik" + nik);
			System.out.println("ini role" + role);
			
			List<String> karyawanFix = new ArrayList<String>();
			boolean manajerStaf = false;
			String status = "";
		// manajer staf dan nonstaf
			if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit"))
			{
				List<String> bawahan = listBawahan();
				if (!bawahan.isEmpty()) {
					System.out.println(bawahan);
					for (int i = 0; i < bawahan.size(); i++) {
						if (isStaf(bawahan.get(i)) == true) {
							manajerStaf = true;
							break;
						}
					}
					karyawanFix = bawahan;
				}
				if (manajerStaf == true) {
					return "redirect:/login"; 
				}
				
				List<KPI> sesuaiPeriode = sesuaiPeriodeKPI();
				List<KPI> kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
				System.out.println(kpiDilihat.size());
				System.out.println(kpiDilihat);	
				boolean idValid = false;
				//cek apakah id yg di parameter sesuai sama id bawahan pada periode yang benar
				for (int i = 0; i < kpiDilihat.size(); i++) {
					if (id.equals(kpiDilihat.get(i).getId())) {
						idValid = true;
					}
				}
				
				if (idValid == false) {
					return "redirect:/login"; 
				}
				KPI kpiStatus = kpiDAO.getKPIbyId(id);
				
				String mgr_approver = kpiStatus.getMgr_approver();
				if (mgr_approver != null && mgr_approver.equals("REJECTED")) {
					status = "Rejected";
				} else if (mgr_approver == null) {
					status = "Waiting for Approval";
				} else {
					status = "Approved";
				}
				//coba
				kpiStatus.setStatus(status);
				System.out.println("====================STATUS TELAH DISET DI CONTROLLER================");
				System.out.println(kpiStatus.getStatus());
			// HC	
			} else if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
				List<String> karyawan = listKaryawan();
				if (!karyawan.isEmpty()) {
					System.out.println(karyawan);
					karyawanFix = karyawan;
				}
				List<KPI> sesuaiPeriode = sesuaiPeriodeKPI();
				List<KPI> kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
				System.out.println(kpiDilihat.size());
				System.out.println(kpiDilihat);	
				boolean idValid = false;
				//cek apakah id yg di parameter sesuai sama id bawahan pada periode yang benar
				for (int i = 0; i < kpiDilihat.size(); i++) {
					if (id.equals(kpiDilihat.get(i).getId())) {
						idValid = true;
					}
				}
				if (idValid == false) {
					return "redirect:/login"; 
				}
				KPI kpiStatus = kpiDAO.getKPIbyId(id);
				
				String hc_approver = kpiStatus.getHc_approver();
				if (hc_approver != null && hc_approver.equals("REJECTED")) {
					status = "Rejected";
				} else if (hc_approver == null) {
					status = "Waiting for Approval";
				} else {
					status = "Approved";
				}
			} else {
				return "redirect:/login"; 
			}
		
		KPI kpi = kpiDAO.getKPIbyId(id);
		Karyawan karyawan = kpiDAO.getKaryawanByNIK(kpi.getId_manajer());
		String divisi = kpiDAO.getDivisi(karyawan.getSto_id());
		String unit = kpiDAO.getUnit(karyawan.getSto_id());
		karyawan.setDivisi(divisi);
		karyawan.setUnit(unit);
		List<KPI_Detail> kpi_komponen = kpiDAO.getKPIDetails(id);
		System.out.println(kpi_komponen.size());
		System.out.println(kpi_komponen);
		
		System.out.println("=======================STATUS DARI CONTROLLER==============================");
		System.out.println(status);
		
		model.addAttribute("model", kpi_komponen);
		model.addAttribute("id_kpi", id);
		model.addAttribute("status", status);
		model.addAttribute("karyawan", karyawan);
		return "KPI-DraftApproval";
	}
	
	@RequestMapping("/KPI/Draft/View/Submit/Approve/{id}")
	public String KPIApprovalSubmitApprove(Model model, HttpSession session, Principal principal,  
			@PathVariable(value = "id") String id) 
	{
		
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		KPI kpi = kpiDAO.getKPIbyId(id);
		
		if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
			kpi.setHc_approver(nik);
			kpiDAO.approveKPIByHC(kpi);
			
		} else if (role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit") || role.equalsIgnoreCase("Head Division")) {
			kpi.setMgr_approver(nik);
			kpiDAO.approveKPIByVP(kpi);
			
		} else if (role.equalsIgnoreCase("User") && !isStaf(nik)) {
			kpi.setMgr_approver(nik);
			kpiDAO.approveKPIByVP(kpi);

		} else {
			return "redirect:/login"; 
		}
		kpiDAO.addLog(nik, "Approve KPI dengan ID"+id);
		String status = "=============================STATUS=============================";
		System.out.println(id);
		System.out.println(status);
		
		
		return "redirect:/KPI/Draft/View/"+id;	
	}

	@RequestMapping("/KPI/Draft/View/Submit/Reject/{id}")
	public String KPIApprovalSubmitReject(Model model, Principal principal, HttpSession session,  
			@PathVariable(value = "id") String id) 
	{
		
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		KPI kpi = kpiDAO.getKPIbyId(id);
		String rejected = "REJECTED";
		
		if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
			kpi.setHc_approver(rejected);
			kpiDAO.approveKPIByHC(kpi);
			
		} else if (role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit") || role.equalsIgnoreCase("Head Division")) {
			kpi.setMgr_approver(rejected);
			kpiDAO.approveKPIByVP(kpi);
			
		} else if (role.equalsIgnoreCase("User") && !isStaf(nik)) {
			kpi.setMgr_approver(rejected);
			kpiDAO.approveKPIByVP(kpi);

		} else {
			return "redirect:/login"; 
		}
		
		kpiDAO.addLog(nik, "Reject KPI dengan ID"+id);
		String status = "=============================STATUS=============================";
		System.out.println(id);
		System.out.println(status);
		
		
		return "redirect:/KPI/Draft/View/"+id;	
	}
	
	@RequestMapping(value="/KPI/Draft/Edit/Submit", method = RequestMethod.GET)
	public String changeKPI_DetailSubmit(Model model, @ModelAttribute KPI_Detail kpi_detail) {

		System.out.println("MASUK");
		System.out.println(kpi_detail);
		
		kpiDAO.updateKPI_Detail(kpi_detail);
		kpiDAO.addLog(getNIK(), "Edit Indikator KPI dengan ID"+kpi_detail.getId());
		return "redirect:/KPI/Personal";
	}
	
	@RequestMapping(value="/KPI/Draft/Delete/Submit/{id}")
	public String deleteKPI_DetailSubmit(Model model, @PathVariable(value = "id") String id) {
	
		System.out.println("MASUK DELETE");
		System.out.println("ini id kpidetnyaaa"); 
		KPI_Detail kpiDelete = kpiDAO.getKPIDetailById(id);
		System.out.println("ini kpidet yg mau diapuss");
		System.out.println(kpiDelete);
		kpiDAO.deleteKPI_Detail(kpiDelete);
		kpiDAO.addLog(getNIK(), "Delete Indikator KPI dengan ID"+id);
		
		return "redirect:/KPI/Personal";
	}
	
	
	@RequestMapping("/KPI/Evaluation/Manager")
	public String selectKPIEvaluationManager(Model model, Principal principal, HttpSession session) {
		/**
		 *
		 * milihNilaiManajer role:manajer non staf-- pemilihan manajer

		 * */
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		List<String> karyawanFix = new ArrayList<String>();
		boolean manajerStaf = false;
		List<KPI> sesuaiPeriode = new ArrayList<KPI>();
		List<KPI> kpiDilihat = new ArrayList<KPI>();
		
		
		// manajer staf dan nonstaf
		if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit"))
		{
			List<String> bawahan = listBawahan();
			if (!bawahan.isEmpty()) {
				System.out.println(bawahan);
				for (int i = 0; i < bawahan.size(); i++) {
					if (isStaf(bawahan.get(i)) == true) {
						manajerStaf = true;
						break;
					}
				}
				karyawanFix = bawahan;
			}
			if (manajerStaf == true) {
				return "redirect:/KPI/Evaluation/Staff"; 
			}
			
			sesuaiPeriode = sesuaiPeriodeKPI();
			kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
			System.out.println(kpiDilihat.size());
			System.out.println(kpiDilihat);
			
			//setting status
			for (int i = 0; i < kpiDilihat.size(); i++) {
				KPI kpiStatus = kpiDAO.getKPIbyId(kpiDilihat.get(i).getId());
				String status = "";
				String karyawan_approver = kpiStatus.getKaryawan_approver_nilai();
				String hc_approver = kpiStatus.getHc_approver_nilai();
				if (karyawan_approver != null && karyawan_approver.equals("REJECTED")) {
					status = "Rejected By Approver";
				} else if (hc_approver != null && hc_approver.equals("REJECTED")) {
					status = "Rejected By Approver";
				} else if (kpiStatus.getNilai() == 0) {
					status = "Waiting for Grading";
				} else {
					status = "Graded";
				}
				//mengganti status
				kpiDilihat.get(i).setStatus(status);
				System.out.println("====================STATUS TELAH DISET DI CONTROLLER================");
				System.out.println(kpiDilihat.get(i).getStatus());
				
			}

		} else {
			return "redirect:/login"; 
		}
		model.addAttribute("model", kpiDilihat);
	
		return "KPI-ManagerSelection";
	}
	
	@RequestMapping("/KPI/Evaluation/Manager/{id}")
	public String KPIEvaluationManager(Model model, Principal principal, @PathVariable(value = "id") String id, HttpSession session) {
		
		/**
		 *
		 * makeNilaiManajer role: non manajer-- penilaian manajer
		 * */
		//kalo manajer staf, ambil id dari kpi dia sendiri
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		List<String> karyawanFix = new ArrayList<String>();
		boolean manajerStaf = false;
		
		// manajer staf dan nonstaf
			if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit"))
			{
				List<String> bawahan = listBawahan();
				if (!bawahan.isEmpty()) {
					System.out.println(bawahan);
					for (int i = 0; i < bawahan.size(); i++) {
						if (isStaf(bawahan.get(i)) == true) {
							manajerStaf = true;
							break;
						}
					}
					karyawanFix = bawahan;
				}
				if (manajerStaf == true) {
					return "redirect:/login"; 
				}
				
				List<KPI> sesuaiPeriode = sesuaiPeriodeKPI();
				List<KPI> kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
				System.out.println(kpiDilihat.size());
				System.out.println(kpiDilihat);	
				boolean idValid = false;
				//cek apakah id yg di parameter sesuai sama id bawahan pada periode yang benar
				for (int i = 0; i < kpiDilihat.size(); i++) {
					if (id.equals(kpiDilihat.get(i).getId())) {
						idValid = true;
					}
				}
				if (idValid == false) {
					return "redirect:/login"; 
				}
			// HC	
			} else if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
				List<String> karyawan = listKaryawan();
				if (!karyawan.isEmpty()) {
					System.out.println(karyawan);
					karyawanFix = karyawan;
				}
				List<KPI> sesuaiPeriode = sesuaiPeriodeKPI();
				List<KPI> kpiDilihat = kpiDilihat(sesuaiPeriode, karyawanFix);
				System.out.println(kpiDilihat.size());
				System.out.println(kpiDilihat);	
				boolean idValid = false;
				//cek apakah id yg di parameter sesuai sama id bawahan pada periode yang benar
				for (int i = 0; i < kpiDilihat.size(); i++) {
					if (id.equals(kpiDilihat.get(i).getId())) {
						idValid = true;
					}
				}
				if (idValid == false) {
					return "redirect:/login"; 
				}
			} else {
				return "redirect:/login"; 
			}
		List<KPI_Detail> kpi_komponen = kpiDAO.getKPIDetails(id);
		System.out.println(kpi_komponen.size());
		System.out.println(kpi_komponen);
		KPI kpi = kpiDAO.getKPIbyId(id);
		Karyawan karyawan = kpiDAO.getKaryawanByNIK(kpi.getId_manajer());
		String divisi = kpiDAO.getDivisi(karyawan.getSto_id());
		String unit = kpiDAO.getUnit(karyawan.getSto_id());
		karyawan.setDivisi(divisi);
		karyawan.setUnit(unit);
		model.addAttribute("karyawan", karyawan);
		model.addAttribute("nilai_akhir", kpi.getNilai());
		model.addAttribute("model", kpi_komponen);
		return "KPI-ManagerEvaluation";
	}
	
		
	@RequestMapping(value="KPI/Evaluation/Manager/Submit", method = RequestMethod.GET)
	public String KPIEvaluationManagerSubmit(@ModelAttribute KPI_Detail kpi_detail, Model model) {
		System.out.println("MASUK");
		System.out.println(kpi_detail);
		
		kpiDAO.makeDetailNilai(kpi_detail);
		
		//mengubah nilai akhir di KPI
		int id_kpi = kpi_detail.getId_kpi();
		KPI kpiDihitung = kpiDAO.getKPIbyId(Integer.toString(id_kpi));
		List<KPI_Detail> kpiDihitung_details = kpiDihitung.getKomponen_kpi();
		
		float nilai_akhir_fix = 0;
		float nilai_akhir_sementara = 0;
		for (int i = 0; i < kpiDihitung_details.size(); i++) {
			float nilai = kpiDihitung_details.get(i).getNilai_hasil();
			float bobot = kpiDihitung_details.get(i).getBobot();
					
			nilai_akhir_sementara = hitungNilaiAkhir(nilai, bobot);
			nilai_akhir_fix += nilai_akhir_sementara;
		}
		String id_kpi_string = Integer.toString(id_kpi);
		String nilai_akhir_fix_string = Float.toString(nilai_akhir_fix);
		System.out.println("================PERHITUNGAN NILAI AKHIR=================");
		System.out.println(id_kpi_string);
		System.out.println(nilai_akhir_fix_string);
		kpiDAO.makeNilai(id_kpi_string, nilai_akhir_fix_string);
		kpiDAO.addLog(getNIK(), "Update Nilai KPI "+id_kpi);
		return "redirect:/KPI/Evaluation/Manager/"+id_kpi;
	} 
	
	@RequestMapping("/KPI/Evaluation/Staff")
	public String KPIEvaluationStaff(Model model, Principal principal, HttpSession session) {
		/**
		 *
		 * makeNilaiStaf role: manajer -- kpi-staf ess
		 * hanya jika dia manajer dengan staf yang tidak memiliki bawahan lagi

		 * */
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		List<String> karyawanFix = new ArrayList<String>();
		boolean manajerStaf = false;
		String id = "-";
		// manajer staf dan nonstaf
			if (role.equalsIgnoreCase("Head Division") || role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit"))
			{
				List<String> bawahan = listBawahan();
				if (!bawahan.isEmpty()) {
					System.out.println(bawahan);
					for (int i = 0; i < bawahan.size(); i++) {
						if (isStaf(bawahan.get(i)) == true) {
							manajerStaf = true;
							break;
						}
					}
					karyawanFix = bawahan;
				}
				if (manajerStaf == false) {
					return "redirect:/login"; 
				}
				
				String periodenow = kpiDAO.periodePenilaianAktif();
				System.out.println("periodenow"+periodenow);
				//cari kpi berdasarkan nik
				List<KPI> kpi = kpiDAO.getKPI(nik);
				System.out.println(kpi);
				for (int i = 0; i < kpi.size(); i++) {
					System.out.println(kpi.get(i).getId_manajer() + " per:"+ kpi.get(i).getId_periode_penilaian() + " id:" +kpi.get(i).getId());
				}
				
				//dlm satu periode hanya memiliki 1 kpi
				//mencari kpi yg aktif
				String idKPI = "";
				for (int i = 0; i < kpi.size(); i++) {
					System.out.println(kpi.get(i).getId_periode_penilaian());
					if (kpi.get(i).getId_periode_penilaian().equals(periodenow)) {
						idKPI = kpi.get(i).getId();
						System.out.println("kpi:"+idKPI);
					}
				}
				//id pada kpi_karyawan yg akan dinilai
				id = idKPI;
				
			} else {
				return "redirect:/login"; 
			}
		
		List<KPI_Karyawan> kpiDilihat = new ArrayList<KPI_Karyawan>();
		
		for (int i = 0; i < karyawanFix.size(); i++) {
			String nik_karyawan = karyawanFix.get(i);
			KPI_Karyawan baru = kpiDAO.getKPIKaryawan(id, nik_karyawan);
			if (baru == null) {
				baru = new KPI_Karyawan();
				baru.setId_kpi(id);
				baru.setNik_pegawai(nik_karyawan);
				baru.setStatus("Waiting for Grading");
				kpiDAO.makeKPIKaryawan(id, nik_karyawan);
			} else {
				String karyawan_approver = baru.getStaf_approver_nilai();
				String hc_approver = baru.getHc_approver_nilai();
				if (karyawan_approver != null && karyawan_approver.equals("REJECTED")) {
					baru.setStatus("Rejected by Approver");
					
				} else if (hc_approver != null && hc_approver.equals("REJECTED")) {
					baru.setStatus("Rejected by Approver");
				}
			}
			String nama_karyawan = kpiDAO.searchPegawai(nik_karyawan);
			baru.setNama_karyawan(nama_karyawan);
			kpiDilihat.add(baru);
		}
		
		System.out.println(kpiDilihat);
		model.addAttribute("model", kpiDilihat);
		return "KPI-StaffEvaluation";
	}
	
	
	@RequestMapping("/KPI/Evaluation/Staff/Submit")
	public String KPIEvaluationStaffSubmit(@ModelAttribute KPI_Karyawan kpi_karyawan, Model model) {
		
		System.out.println("MASUK");
		System.out.println(kpi_karyawan);
		kpi_karyawan.setStaf_approver_nilai(null);
		kpi_karyawan.setHc_approver_nilai(null);
		System.out.println("=============MAKE NILAI STAF=================");
		kpiDAO.makeNilaiStaf(kpi_karyawan);
		kpiDAO.approveNilaiByKaryawan(kpi_karyawan);
		kpiDAO.approveNilaiKaryawanByHC(kpi_karyawan);
		System.out.println(kpi_karyawan);
		kpiDAO.addLog(getNIK(), "Update Nilai KPI Karyawan"+kpi_karyawan.getNik_pegawai());
		return "redirect:/KPI/Evaluation/Staff";
	}
	
	@RequestMapping("/KPI/MyResult/Submit/Approve/{id}")
	public String approveNilaiResultByKaryawanSubmitApprove(Principal principal, @PathVariable(value = "id") String id, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		if (role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit") || role.equalsIgnoreCase("Head Division")) {
			KPI kpi = kpiDAO.getKPIbyId(id);
			kpi.setKaryawan_approver_nilai(nik);
			kpiDAO.approveNilaiByManager(kpi);
		} else if (role.equalsIgnoreCase("User")) {
			KPI_Karyawan kpi_karyawan = kpiDAO.getKPIKaryawan(id, nik);
			kpi_karyawan.setStaf_approver_nilai(nik);
			kpiDAO.approveNilaiByKaryawan(kpi_karyawan);
		} else {
			return "redirect:/login"; 
		}
		kpiDAO.addLog(getNIK(), "Approve Nilai KPI by Karyawan");
		String status = "=============================STATUS=============================";
		System.out.println(id);
		System.out.println(status);
		return "redirect:/KPI/Personal";
	}
	
	@RequestMapping("/KPI/MyResult/Submit/Reject/{id}")
	public String approveNilaiResultByKaryawanSubmitReject(Principal principal,  
			@PathVariable(value = "id") String id, HttpSession session) {
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		String approverName = "REJECTED";
		
		if (role.equalsIgnoreCase("BOD") || role.equalsIgnoreCase("Head Unit") || role.equalsIgnoreCase("Head Division")) {
			KPI kpi = kpiDAO.getKPIbyId(id);
			kpi.setKaryawan_approver_nilai(approverName);
			kpiDAO.approveNilaiByManager(kpi);
		} else if (role.equalsIgnoreCase("User")) {
			KPI_Karyawan kpi_karyawan = kpiDAO.getKPIKaryawan(id, nik);
			kpi_karyawan.setStaf_approver_nilai(approverName);
			kpiDAO.approveNilaiByKaryawan(kpi_karyawan);
		} else {
			return "redirect:/login"; 
		}
		kpiDAO.addLog(getNIK(), "Reject Nilai KPI by Karyawan");
		String status = "=============================STATUS=============================";
		System.out.println(id);
		System.out.println(status);
		return "redirect:/KPI/Personal";
	}
	
	@RequestMapping("/KPI/HCResult")
	public String approveNilaiResultByHC(Model model, Principal principal, HttpSession session) {
		/**
		 *
		 * approveNilaiByHC role: HC -- kpi hasil pms

		 * */
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		String periodenow = kpiDAO.periodePenilaianAktif();
		System.out.println("periodenow"+periodenow);
		
		List<String> karyawanStaf = new ArrayList<String>();
		List<String> karyawanManajer = new ArrayList<String>();
		
		if (role.equalsIgnoreCase("HC HO") || role.equalsIgnoreCase("HC Unit")) {
			List<String> karyawan = listKaryawan();
			System.out.println(karyawan);
			for (int i = 0; i< karyawan.size(); i++) {
				if (isStaf(karyawan.get(i))) {
					karyawanStaf.add(karyawan.get(i));
				} else {
					karyawanManajer.add(karyawan.get(i));
				}
			}
			System.out.println(karyawanStaf);
			System.out.println(karyawanManajer);
			
		} else {
			return "redirect:/login"; 
		}
		boolean approvalDone = true;
		//udah dapet perbedaan karyawan yang staf dan manajer
		//jika staf, kpinya ngikutin kpi atasannya
		//jika manajer, kpinya adalah kpinya sendiri
		
		List<KPI> kpiAll = new ArrayList<KPI>();
		
		//mencari semua kpi sesuai periode
		List<KPI> sesuaiPeriode = sesuaiPeriodeKPI();
		
		List<KPI> kpiDilihatStaf = kpiDilihat(sesuaiPeriode, karyawanStaf);
		List<KPI_Karyawan> kpiStaf = kpiKaryawanDilihat(sesuaiPeriode, karyawanStaf);
		List<KPI> kpiDilihatManajer = kpiDilihat(sesuaiPeriode, karyawanManajer);
		
		System.out.println("=============KPI STAF====================");
		System.out.println(kpiDilihatStaf);
		System.out.println("=============KPI MANAJER====================");
		System.out.println(kpiDilihatManajer);
		
		//mengisi kpi staf
		if (kpiDilihatStaf.size() != 0) {
			for (int i = 0; i < kpiStaf.size(); i++)	{
				KPI_Karyawan karyawan = kpiStaf.get(i);
				String id_kpi = karyawan.getId_kpi();
				KPI kpi = kpiDAO.getKPIbyId(id_kpi);
				kpi.setNama_manajer(karyawan.getNama_karyawan());
				kpi.setNilai(Float.parseFloat(karyawan.getNilai()));
				if (kpi.getHc_approver_nilai() == null) {
					kpi.setHc_approver_nilai("Waiting for Approval");
					approvalDone = false;
				} else if (kpi.getHc_approver_nilai().equals("REJECTED")) {
					kpi.setHc_approver_nilai("Rejected");
				} else {
					kpi.setHc_approver_nilai("Approved");
				}
				kpi.setNama_karyawan(karyawan.getNama_karyawan());
				kpiAll.add(kpi);
			}
		}
		
		
		//mengisi kpi manajer
		if (kpiDilihatManajer.size() != 0) {
			for (int i = 0; i < kpiDilihatManajer.size(); i++) {
				KPI kpi = kpiDilihatManajer.get(i);
				if (kpi.getHc_approver_nilai() == null) {
					kpi.setHc_approver_nilai("Waiting for Approval");
					approvalDone = false;
				} else if (kpi.getHc_approver_nilai().equals("REJECTED")) {
					kpi.setHc_approver_nilai("Rejected");
				} else {
					kpi.setHc_approver_nilai("Approved");
				}
				kpiAll.add(kpi);
			}
		}
		
		System.out.println("=============KPI ALL====================");
		System.out.println(kpiAll);
		
		
		for (int i = 0; i < kpiAll.size(); i++) {
			List<KPI_Detail> kpi_detail = kpiDAO.getKPIDetailByIdKPI(kpiAll.get(i).getId());
			kpiAll.get(i).setKomponen_kpi(kpi_detail);
		}
		
		model.addAttribute("kpiAll", kpiAll);
		System.out.println("=============KPI ALL ADDED KOMPONEN====================");
		System.out.println(kpiAll);
		model.addAttribute("approvalDone", approvalDone);
		return "KPI-HCResult";
	}
	
	@RequestMapping(value="/KPI/HCResult/Submit", method = RequestMethod.GET)
	public String approveNilaiResultByHCSubmit(Principal principal, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
		
		String roleValid = isRoleValid(principal);
		System.out.println(roleValid);
		String nik = getNIK();
		String role = getRole(session);
		System.out.println("ini nik" + nik);
		System.out.println("ini role" + role);
		
		System.out.println("masuk");
		String[] approval_list = request.getParameter("approval_list").split(",");
		System.out.println(approval_list);
		System.out.println("===================MASUK SUBMIT=====================");
		for (int i = 0; i < approval_list.length; i++) {
			System.out.println(approval_list[i]);
			String[] parts = approval_list[i].split(":");
			String nik_pegawai = parts[0]; // 004
			String hasil = parts[1]; // 034556
			System.out.println(nik_pegawai);
			System.out.println(hasil);
			boolean isStaf = isStaf(nik_pegawai);
			List<KPI> kpiList = kpiDAO.getKPI(nik_pegawai);
			String periodenow = kpiDAO.periodePenilaianAktif();
			String id_kpi = "-";
			for (int j = 0; j < kpiList.size(); j++) {
				if (kpiList.get(j).getId_periode_penilaian().equals(periodenow)) {
					id_kpi = kpiList.get(j).getId();
					System.out.println("id_kpi:"+id_kpi);
				}
	
			}
			if(hasil.equals("true")) {
				System.out.println("=================APPROVED BY HC======================");
				if (isStaf == true) {
					System.out.println("DIA STAF");
					KPI_Karyawan kpi_karyawan = kpiDAO.getKPIKaryawan(id_kpi, nik_pegawai);
					kpi_karyawan.setHc_approver_nilai(nik);
					System.out.println(kpi_karyawan);
					kpiDAO.approveNilaiKaryawanByHC(kpi_karyawan);
					kpiDAO.addLog(getNIK(), "Approve Nilai by HC KPI Karyawan "+kpi_karyawan.getNik_pegawai());
				} else {
					System.out.println("DIA MANAGER");
					KPI kpi = kpiDAO.getKPIbyId(id_kpi);
					kpi.setHc_approver_nilai(nik);
					System.out.println(kpi);
					kpiDAO.approveNilaiByHC(kpi);
					kpiDAO.addLog(getNIK(), "Approve Nilai by HC KPI Karyawan "+kpi.getId_manajer());
				}
			} else if (hasil.equals("false")) {
				System.out.println("=================REJECTED BY HC======================");
				if (isStaf == true) {
					System.out.println("DIA STAF");
					KPI_Karyawan kpi_karyawan = kpiDAO.getKPIKaryawan(id_kpi, nik_pegawai);
					kpi_karyawan.setHc_approver_nilai("REJECTED");
					System.out.println(kpi_karyawan);
					kpiDAO.approveNilaiKaryawanByHC(kpi_karyawan);
					kpiDAO.addLog(getNIK(), "Reject Nilai by HC KPI Karyawan "+kpi_karyawan.getNik_pegawai());
				} else {
					System.out.println("DIA MANAGER");
					KPI kpi = kpiDAO.getKPIbyId(id_kpi);
					kpi.setHc_approver_nilai("REJECTED");
					System.out.println(kpi);
					kpiDAO.approveNilaiByHC(kpi);
					kpiDAO.addLog(getNIK(), "Reject Nilai by HC KPI Karyawan "+kpi.getId_manajer());
				}
			}
		}
		return "redirect:/KPI/HCResult";
		
	}
	

}
