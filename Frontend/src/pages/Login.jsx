import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../services/authService";
import "./AuthPage.css";
import axios from "axios";
import { GoogleLogin } from "@react-oauth/google";

function Login() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };




const handleSubmit = async (e) => {
  e.preventDefault();

  setError("");
  setLoading(true);

  try {
    const response = await login(formData);

    console.log("Login response:", response);

    const data = response.data;
    const accessToken = data.accessToken;
    const user = data.user;

    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("user", JSON.stringify(user));

    if (user && user.id != null) {
      localStorage.setItem("userId", String(user.id));
    }

    console.log("Saved user:", user);
    navigate("/");
  } catch (err) {
    console.error(err);

    setError(
      err.response?.data?.message ||
      "Email hoặc mật khẩu không chính xác"
    );
  } finally {
    setLoading(false);
  }
};

// Hàm này phải nằm ngoài handleSubmit

const handleGoogleSuccess = async (credentialResponse) => {
  const credential = credentialResponse?.credential;

  if (!credential) {
    setError("Không nhận được Google ID Token. Vui lòng thử lại.");
    return;
  }

  setError("");
  setLoading(true);

  try {
    const response = await axios.post(
      `${import.meta.env.VITE_API_URL}/auth/google`,
      { credential },
      { withCredentials: true }
    );

    console.log("Google login response:", response.data);

    // Backend trả ApiResponse<LoginResponseDTO>
    const result = response.data?.data;

    if (!result?.accessToken || !result?.user) {
      throw new Error("Dữ liệu đăng nhập Google trả về không hợp lệ.");
    }

    localStorage.setItem("accessToken", result.accessToken);
    localStorage.setItem("user", JSON.stringify(result.user));

    if (result.user.id != null) {
      localStorage.setItem("userId", String(result.user.id));
    }

    navigate("/");
  } catch (err) {
    console.error("Google login failed:", err);
    console.error("Backend response:", err.response?.data);

    setError(
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      "Đăng nhập Google thất bại."
    );
  } finally {
    setLoading(false);
  }
};




  return (
    <div className="auth-wrapper">
      <div className="auth-card-container">
        {/* Banner bên trái */}
        <div className="auth-banner-side">
          <div className="brand-logo">
            <div className="brand-icon">S</div>
            <span className="brand-title">SKATEBOARD</span>
          </div>
          <div className="banner-content">
            <p className="banner-subtitle">STEP INTO YOUR STYLE</p>
            <h1 className="banner-heading">
              Find your <br />
              perfect shoes.
            </h1>
            <p className="banner-description">
              Khám phá bộ sưu tập giày thể thao cá tính và phong cách dành riêng cho bạn.
            </p>
          </div>
          <div></div>
        </div>

        {/* Form bên phải */}
        <div className="auth-form-side">
          <div className="form-header">
            <p className="form-category">SKATEBOARD ACCOUNT</p>
            <h2 className="form-title">Welcome back</h2>
            <p className="form-subtext">
              Đăng nhập để tiếp tục trải nghiệm mua sắm của bạn.
            </p>
          </div>

          {error && <div className="error-banner">{error}</div>}

          <form onSubmit={handleSubmit} className="styled-form">
            <div className="input-block">
              <label>Email</label>
              <input
                type="email"
                name="username"
                placeholder="researcher@example.com"
                value={formData.username}
                onChange={handleChange}
                required
                className="styled-input"
              />
            </div>

            <div className="input-block">
              <label>Mật khẩu</label>
              <div className="input-wrapper">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Nhập mật khẩu"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="styled-input"
                />
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "👁️‍🗨️" : "👁️"}
                </button>
              </div>
            </div>

            <div className="form-row-helpers">
              <label className="remember-me">
                <input type="checkbox" /> Ghi nhớ thiết bị
              </label>
              <a href="#forgot" className="forgot-link">
                Quên mật khẩu?
              </a>
            </div>

            <button type="submit" className="submit-btn" disabled={loading}>
              {loading ? "Đang xử lý..." : "Đăng nhập"}
            </button>
          </form>

          <div className="divider">
            <span>HOẶC</span>
          </div>

          <div className="google-login-container">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => {
                setError("Không thể đăng nhập bằng Google.");
              }}
              text="continue_with"
              shape="rectangular"
              width="320"
            />
          </div>

          <p className="switch-auth-text">
            Chưa có tài khoản?
            <button
              type="button"
              className="switch-btn"
              onClick={() => navigate("/auth/register")}
            >
              Tạo tài khoản mới
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;