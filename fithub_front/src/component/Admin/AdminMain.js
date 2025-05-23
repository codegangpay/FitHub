import {
  Link,
  NavLink,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";
import AdminAds from "./AdminAds";
import "./admin.css";
import AdminMember from "./AdminMember";
import AdminStat from "./AdminStat";
import AdminChat from "./AdminChat";
import { useEffect } from "react";
import { useRecoilState } from "recoil";
import { logoutState, memberState } from "../utils/RecoilData";
import AdminGoods from "./AdminGoods";
import Swal from "sweetalert2";

const AdminMain = () => {
  const [logoutST, setLogoutST] = useRecoilState(logoutState);
  const [memberInfo, setMemberInfo] = useRecoilState(memberState);
  const location = useLocation(); // 현재 경로 정보 가져오기
  const navigate = useNavigate();
  // 관리자(member_level === 1)만 접근 가능하도록 설정
  if (logoutST) {
    navigate("/");
    setLogoutST(false);
  } else {
    if (!memberInfo || memberInfo.memberLevel !== 1) {
      navigate("/");
      Swal.fire({
        title: "입장 불가",
        text: "관리자만 입장 가능합니다.",
        icon: "warning",
        confirmButtonColor: "#589c5f",
        confirmButtonText: "확인",
      }).then((result) => {
        if (result.isConfirmed) {
          navigate("/");
        }
      });
    }
  }

  useEffect(() => {
    if (!memberInfo) {
      navigate("/");
    }
  }, []);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location]);

  return (
    <>
      {memberInfo && memberInfo?.memberLevel === 1 ? (
        <section className="section admin-section">
          <div className="navi-bar">
            <Sidebar />
          </div>
          <div className="section-page">
            <Routes>
              <Route path="member" element={<AdminMember />} />
              <Route path="stat" element={<AdminStat />} />
              <Route path="chat" element={<AdminChat />} />
              <Route path="Ads" element={<AdminAds />} />
              <Route path="goods" element={<AdminGoods />} />
            </Routes>
          </div>
        </section>
      ) : null}
    </>
  );
};

// 사이드바 컴포넌트
const Sidebar = () => {
  return (
    <div className="side-menu">
      <NavLink
        to="/admin/member"
        className={({ isActive }) => (isActive ? "active-tab" : "")}
      >
        사이트 관리
      </NavLink>
      <NavLink
        to="/admin/stat"
        className={({ isActive }) => (isActive ? "active-tab" : "")}
      >
        사이트 통계 조회
      </NavLink>
      <NavLink
        to="/admin/chat"
        className={({ isActive }) => (isActive ? "active-tab" : "")}
      >
        문의 채팅
      </NavLink>
      <NavLink
        to="/admin/ads"
        className={({ isActive }) => (isActive ? "active-tab" : "")}
      >
        광고 관리
      </NavLink>
      <NavLink
        to="/admin/goods"
        className={({ isActive }) => (isActive ? "active-tab" : "")}
      >
        상품 등록
      </NavLink>
    </div>
  );
};
export default AdminMain;
