package kr.co.fithub.admin.model.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.fithub.admin.model.dao.AdminDao;
import kr.co.fithub.admin.model.dto.AdminDTO;
import kr.co.fithub.admin.model.dto.AdsDTO;
import kr.co.fithub.member.model.dto.MemberDTO;
import kr.co.fithub.util.PageInfo;
import kr.co.fithub.util.PageInfoUtil;

@Service
public class AdminService {
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private PageInfoUtil pageInfoUtil;

	public Map memberList(int memberPage) {
		int numPerPage = 5;
		int pageNaviSize = 5;
		int memberTotalCount = adminDao.memberTotalCount();
		
		PageInfo memberPi = pageInfoUtil.getPageInfo(memberPage, numPerPage, pageNaviSize, memberTotalCount);
		
		List memberList = adminDao.memberList(memberPi);
		
		Map<String, Object> map = new HashMap<>();
		map.put("memberList", memberList);
		map.put("memberPi", memberPi);
		return map;
	}
	
	public Map delMemberList(int delMemberPage) {
		int numPerPage = 5;
		int pageNaviSize = 5;
		int delMemberTotalCount = adminDao.delMemberTotalCount();
		
		PageInfo delMemberPi = pageInfoUtil.getPageInfo(delMemberPage, numPerPage, pageNaviSize, delMemberTotalCount);
		List delMemberList = adminDao.delMemberList(delMemberPi);
		
		Map<String, Object> map = new HashMap<>();
		map.put("delMemberList", delMemberList);
		map.put("delMemberPi", delMemberPi);
		return map;
	}
	
	@Transactional
	public int adminMemberChange(String memberId, MemberDTO memberData) {
		memberData.setMemberId(memberId);
		int result = adminDao.adminMemberChange(memberData);
		return result;
	}
	
	@Transactional
	public int adminMemberDelete(int memberNo, String adminId) {
		MemberDTO m = adminDao.selectOneMember(memberNo);
		int result = 0;
		if(m != null) {			
			result += adminDao.adminDelMemberInsert(m, adminId);
			String uuidSuffix = UUID.randomUUID().toString().substring(0, 8);
		    String newMemberId = m.getMemberId() + "_deleted_" +
		        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
		        "_" + uuidSuffix;
		    result += adminDao.updateMemberKickId(newMemberId, memberNo);
		}
		return result;
	}

	public Map communityList(int communityPage) {
		int numPerPage = 5;
		int pageNaviSize = 5;
		int communityTotalCount = adminDao.communityTotalCount();
		
		PageInfo communityPi = pageInfoUtil.getPageInfo(communityPage, numPerPage, pageNaviSize, communityTotalCount);
		
		List communityList = adminDao.communityList(communityPi);

		Map<String, Object> map = new HashMap<>();
		map.put("communityList", communityList);
		map.put("communityPi", communityPi);
		return map;
	}

	public Map commentList(int commentPage) {
		int numPerPage = 5;
		int pageNaviSize = 5;
		int commentTotalCount = adminDao.commnetTotalCount();
		
		PageInfo commentPi = pageInfoUtil.getPageInfo(commentPage, numPerPage, pageNaviSize, commentTotalCount);
		
		List commentList = adminDao.commentList(commentPi);
		Map<String, Object> map = new HashMap<>();
		map.put("commentList", commentList);
		map.put("commentPi", commentPi);
		return map;
	}
	
	@Transactional
	public int writeAds(AdsDTO ads) {
		int result = adminDao.writeAds(ads);
		return result;
	}

	public List getAdsList() {
		List list = adminDao.getAdsList();
		return list;
	}
	
	public List getAdsType(String adsType) {
		List list = adminDao.getAdsType(adsType);
		return list;
	}

	public AdminDTO getMember() {
		AdminDTO admin = adminDao.getMember();
		return admin;
	}

	public List categoryTotalPrice() {
		List list = adminDao.categoryTotalPrice();
		return list;
	}

	public List totalSell() {
		List list = adminDao.totalSell();
		return list;
	}

	public List weekSales() {
		List list = adminDao.weekSales();
		return list;
	}

	public List monthSales() {
		List list = adminDao.monthSales();
		return list;
	}

	public int deleteAds(int adsNo) {
		int r = adminDao.deleteAds(adsNo);
		return r;
	}

	public int memberRollBack(int memberNo) {
		MemberDTO member = adminDao.selectOneMember(memberNo);
		String memberName = member.getMemberId().split("_")[0];
		member.setMemberId(memberName);
		int r = adminDao.updateMemberId(memberName, memberNo);
		if(r > 0) {
			r += adminDao.deleteDelMember(memberName);
		}
		return r;
	}

}
