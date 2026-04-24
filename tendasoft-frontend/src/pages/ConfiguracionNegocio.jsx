import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  X, Upload, Check, ChevronRight, CheckCircle2, XCircle,
  AlertTriangle, Loader2, Lock
} from 'lucide-react';
import { toast } from 'react-hot-toast';

export default function ConfiguracionNegocioModal({ isOpen, onClose, onConfiguracionGuardada }) {
  const [vista, setVista] = useState('fiscal');
  const [paso, setPaso] = useState('formulario');
  const [loading, setLoading] = useState(false);

  const [logoFile, setLogoFile] = useState(null);
  const [logoPreview, setLogoPreview] = useState(null);
  const [certificadoFile, setCertificadoFile] = useState(null);

  const [datos, setDatos] = useState({
    nombreEmpresa: '',
    cif: '',
    direccion: '',
    mensajeTicket: '',
    verifactuActivado: false,
    rutaLogo: '',
    rutaCertificado: '',
    certificadoPassword: '' // <--- NUEVO
  });

  useEffect(() => {
    if (isOpen) {
      setPaso('formulario');
      axios.get('http://localhost:8080/api/configuracion')
        .then(res => {
          if (res.data) setDatos({ ...res.data, certificadoPassword: '' }); // No bajamos la pass por seguridad
        })
        .catch(err => console.log("Configuración nueva"));
    }
  }, [isOpen]);

  const handleLogoChange = (e) => {
    const file = e.target.files[0];
    if (file) { setLogoFile(file); setLogoPreview(URL.createObjectURL(file)); }
  };

  const handleCertificadoChange = (e) => {
    const file = e.target.files[0];
    if (file) { setCertificadoFile(file); }
  };

  // --- VALIDACIÓN VERIFACTU ---
  const tieneNif = datos.cif && datos.cif.trim() !== '';
  const tieneCertificado = (datos.rutaCertificado && datos.rutaCertificado.trim() !== '') || certificadoFile !== null;
  const tienePassword = datos.certificadoPassword && datos.certificadoPassword.trim() !== '';

  // Requisito real: NIF + Archivo + Password
  const puedeActivarVerifactu = tieneNif && tieneCertificado && tienePassword;

  const handleToggleVerifactu = () => {
    if (!datos.verifactuActivado && !puedeActivarVerifactu) {
      toast.error("Faltan requisitos: NIF, Certificado y Contraseña.");
      return;
    }
    setDatos({ ...datos, verifactuActivado: !datos.verifactuActivado });
  };

  const handleGuardar = async () => {
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('nombreEmpresa', datos.nombreEmpresa);
      formData.append('cif', datos.cif);
      formData.append('direccion', datos.direccion);
      formData.append('mensajeTicket', datos.mensajeTicket);
      formData.append('verifactuActivado', datos.verifactuActivado);
      formData.append('certificadoPassword', datos.certificadoPassword); // <--- ENVIAMOS PASS

      if (logoFile) formData.append('logo', logoFile);
      if (certificadoFile) formData.append('certificado', certificadoFile);

      await axios.post('http://localhost:8080/api/configuracion', formData);

      if (onConfiguracionGuardada) onConfiguracionGuardada();
      setPaso('exito');
    } catch (error) {
      toast.error("Error al guardar la configuración.");
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => { setPaso('formulario'); setVista('fiscal'); onClose(); };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-2xl shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        {paso === 'formulario' && (
          <div className="p-6 flex justify-between items-center border-b border-gray-50">
            <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
              {vista === 'fiscal' ? 'Configuración Fiscal y AEAT' : 'Personalización Ticket'}
            </h2>
            <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors"><X size={24}/></button>
          </div>
        )}

        <div className="p-8">
          {paso === 'formulario' ? (
            <>
              {vista === 'fiscal' ? (
                <div className="space-y-4">
                  <div className="space-y-1">
                    <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Razón Social</label>
                    <input className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold" value={datos.nombreEmpresa} onChange={e => setDatos({...datos, nombreEmpresa: e.target.value})} />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <input placeholder="NIF / CIF" className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold" value={datos.cif} onChange={e => setDatos({...datos, cif: e.target.value})} />
                    <input placeholder="Dirección fiscal" className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold" value={datos.direccion} onChange={e => setDatos({...datos, direccion: e.target.value})} />
                  </div>

                  <div className="p-5 bg-slate-50 border border-slate-200 rounded-2xl my-6">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <p className="font-bold text-[#2C3E50]">Integración VeriFactu</p>
                        <p className="text-xs text-[#95A5A6]">Estado de cumplimiento legal</p>
                      </div>
                      <button onClick={handleToggleVerifactu} className={`w-14 h-8 rounded-full transition-all relative ${datos.verifactuActivado ? 'bg-[#00796B]' : 'bg-gray-300'}`}>
                        <div className={`absolute top-1 bg-white w-6 h-6 rounded-full shadow-sm transition-all ${datos.verifactuActivado ? 'left-7' : 'left-1'}`} />
                      </button>
                    </div>

                    <div className="space-y-2 pt-3 border-t">
                      <div className="flex justify-between text-xs font-bold uppercase">
                        <span>NIF Validado</span> {tieneNif ? <CheckCircle2 size={14} className="text-green-500"/> : <XCircle size={14} className="text-red-400"/>}
                      </div>
                      <div className="flex justify-between text-xs font-bold uppercase">
                        <span>Archivo .p12</span> {tieneCertificado ? <CheckCircle2 size={14} className="text-green-500"/> : <XCircle size={14} className="text-red-400"/>}
                      </div>
                      <div className="flex justify-between text-xs font-bold uppercase">
                        <span>Contraseña</span> {tienePassword ? <CheckCircle2 size={14} className="text-green-500"/> : <XCircle size={14} className="text-red-400"/>}
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <label className="h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-[10px] flex items-center justify-center gap-2 hover:bg-gray-50 cursor-pointer uppercase">
                      <Upload size={16} /> {certificadoFile ? 'CAMBIAR .P12' : 'SUBIR CERTIFICADO'}
                      <input type="file" accept=".p12,.pfx" className="hidden" onChange={handleCertificadoChange} />
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-4 top-4 text-slate-300" size={18} />
                      <input
                        type="password"
                        placeholder="Contraseña"
                        className="w-full h-14 pl-12 pr-4 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-sm"
                        value={datos.certificadoPassword}
                        onChange={e => setDatos({...datos, certificadoPassword: e.target.value})}
                      />
                    </div>
                  </div>

                  <button onClick={() => setVista('ticket')} className="w-full h-14 bg-gray-100 text-[#001D3D] font-black rounded-2xl hover:bg-gray-200 transition-all uppercase text-xs tracking-widest flex items-center justify-center mt-2">
                    Diseño del Ticket <ChevronRight size={18} className="ml-2" />
                  </button>
                </div>
              ) : (
                /* --- VISTA TICKET --- */
                <div className="flex gap-8">
                  <div className="flex-1 space-y-4">
                    <textarea className="w-full h-32 p-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold resize-none" placeholder="Mensaje al pie del ticket..." value={datos.mensajeTicket} onChange={e => setDatos({...datos, mensajeTicket: e.target.value})} />
                    <label className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3 cursor-pointer hover:bg-gray-50">
                      <Upload size={18} /> {logoFile ? 'CAMBIAR LOGO' : 'SUBIR LOGO'}
                      <input type="file" accept="image/*" className="hidden" onChange={handleLogoChange} />
                    </label>
                    <button onClick={() => setVista('fiscal')} className="text-[#00796B] font-black text-[10px] uppercase tracking-widest">Volver a AEAT</button>
                  </div>
                  <div className="w-64 bg-white border-t-8 border-[#001D3D] p-5 shadow-xl text-[10px] font-mono flex flex-col items-center">
                    {(logoPreview || datos.rutaLogo) && <img src={logoPreview || `http://localhost:8080/uploads/${datos.rutaLogo}`} className="w-12 h-12 object-contain mb-2 grayscale" alt="logo"/>}
                    <p className="font-black text-center uppercase">{datos.nombreEmpresa || 'TendaSoft POS'}</p>
                    <p className="opacity-60 text-center mb-4">CIF: {datos.cif || '---'}</p>
                    <div className="w-full border-t border-dashed border-gray-300 pt-2 mb-4">
                      <div className="flex justify-between font-black"><span>1x PRODUCTO</span><span>10,00€</span></div>
                    </div>
                    <div className="w-full border-t-2 border-black pt-2 flex justify-between font-black text-sm"><span>TOTAL</span><span>10,00€</span></div>
                    <p className="mt-4 italic text-center">"{datos.mensajeTicket || 'Gracias por su compra'}"</p>
                    <div className="mt-2 w-10 h-10 bg-slate-100 flex items-center justify-center border text-[8px]">QR</div>
                  </div>
                </div>
              )}

              <button onClick={handleGuardar} disabled={loading} className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl mt-10 hover:bg-[#004D40] transition-all flex items-center justify-center gap-3 shadow-lg">
                {loading ? <Loader2 className="animate-spin" /> : <><Check size={20} /> GUARDAR CONFIGURACIÓN</>}
              </button>
            </>
          ) : (
            /* --- ÉXITO --- */
            <div className="py-10 text-center animate-in zoom-in">
              <div className="w-20 h-20 bg-green-100 text-green-500 rounded-full flex items-center justify-center mb-6 mx-auto"><CheckCircle2 size={40} /></div>
              <h3 className="text-2xl font-black text-slate-800">¡Configurado!</h3>
              <p className="text-slate-500 mt-2 mb-8 font-medium">El sistema VeriFactu está listo para firmar tus ventas.</p>
              <button onClick={resetModal} className="w-full h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-black transition-all uppercase text-xs tracking-widest">Cerrar</button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}