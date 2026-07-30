import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "sonner";
import AdminDashboard from "./pages/AdminDashboard";
import PublicStatusPage from "./pages/PublicStatusPage";

function App() {
  return (
    <BrowserRouter>
      <Toaster theme="dark" position="top-right" richColors />
      <Routes>
        <Route path="/" element={<AdminDashboard />} />
        <Route path="/status" element={<PublicStatusPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
