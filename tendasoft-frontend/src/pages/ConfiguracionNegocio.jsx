import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  X, Upload, Check, ChevronRight, CheckCircle2, XCircle,
  AlertTriangle, Loader2
} from 'lucide-react';

export default function ConfiguracionNegocioModal({ isOpen, onClose, onConfiguracionGuardada }) {
  const [vista, setVista] = useState('fiscal');
  const [paso, setPaso] = useState('formulario'); // 'formulario' | 'exito'
  const [loading, setLoading] = useState(false);

  // Estados para archivos físicos antes de enviar
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
    rutaCertificado: ''
  });

  useEffect(() => {
    if (isOpen) {
      setPaso('formulario'); // Resetear al abrir
      axios.get('http://localhost:8080/api/configuracion')
        .then(res => {
          if (res.data) setDatos(res.data);
        })
        .catch(err => console.log("Configuración no encontrada, se creará una nueva."));
    }
  }, [isOpen]);

  const handleLogoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setLogoFile(file);
      setLogoPreview(URL.createObjectURL(file));
    }
  };

  const handleCertificadoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setCertificadoFile(file);
    }
  };

  // --- LÓGICA DE VALIDACIÓN VERIFACTU ---
  const tieneNif = datos.cif && datos.cif.trim() !== '';
  const tieneCertificado = (datos.rutaCertificado && datos.rutaCertificado.trim() !== '') || certificadoFile !== null;
  const puedeActivarVerifactu = tieneNif && tieneCertificado;

  const handleToggleVerifactu = () => {
    if (!datos.verifactuActivado && !puedeActivarVerifactu) {
      alert("No puedes activar VeriFactu. Faltan requisitos obligatorios (NIF o Certificado Digital).");
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

      if (logoFile) {
        formData.append('logo', logoFile);
      }
      if (certificadoFile) {
        formData.append('certificado', certificadoFile);
      }

      await axios.post('http://localhost:8080/api/configuracion', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      // AVISAMOS A LA TOPBAR DE QUE LOS DATOS HAN CAMBIADO
      if (onConfiguracionGuardada) {
        onConfiguracionGuardada();
      }

      setPaso('exito'); // CAMBIO: Mostramos pantalla de éxito
    } catch (error) {
      console.error(error);
      alert("Error al guardar la configuración.");
    } finally {
      setLoading(false);
    }
  };

  const resetModal = () => {
    setPaso('formulario');
    setVista('fiscal');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-2xl shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        {/* CABECERA (Se oculta en pantalla de éxito para seguir el estilo de las impresoras) */}
        {paso === 'formulario' && (
          <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50">
            <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
              {vista === 'fiscal' ? 'Datos fiscales y AEAT' : 'Configuración del ticket'}
            </h2>
            <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
              <X size={24} className="text-[#001D3D]" />
            </button>
          </div>
        )}

        <div className="p-8">
          {paso === 'formulario' ? (
            <>
              {vista === 'fiscal' ? (
                /* --- VISTA DATOS FISCALES --- */
                <div className="space-y-4">
                  <div className="space-y-1">
                    <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Razón Social</label>
                    <input
                      placeholder="Razón social (nombre legal)"
                      className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50] transition-all"
                      value={datos.nombreEmpresa}
                      onChange={e => setDatos({...datos, nombreEmpresa: e.target.value})}
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">NIF / CIF</label>
                      <input
                        placeholder="NIF / CIF"
                        className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                        value={datos.cif}
                        onChange={e => setDatos({...datos, cif: e.target.value})}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Dirección</label>
                      <input
                        placeholder="Dirección fiscal"
                        className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
                        value={datos.direccion}
                        onChange={e => setDatos({...datos, direccion: e.target.value})}
                      />
                    </div>
                  </div>

                  {/* --- SECCIÓN INTELIGENTE VERIFACTU --- */}
                  <div className="p-5 bg-slate-50 border border-slate-200 rounded-2xl my-6">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <p className="font-bold text-[#2C3E50]">Integración AEAT (VeriFactu)</p>
                        <p className="text-xs text-[#95A5A6]">Envío automático e inalterable de facturas</p>
                      </div>
                      <button
                        onClick={handleToggleVerifactu}
                        className={`w-14 h-8 rounded-full transition-all relative ${
                          datos.verifactuActivado ? 'bg-[#00796B]' : 'bg-gray-300'
                        } ${!datos.verifactuActivado && !puedeActivarVerifactu ? 'opacity-50 cursor-not-allowed' : ''}`}
                        title={!puedeActivarVerifactu && !datos.verifactuActivado ? 'Faltan requisitos' : ''}
                      >
                        <div className={`absolute top-1 bg-white w-6 h-6 rounded-full shadow-sm transition-all ${datos.verifactuActivado ? 'left-7' : 'left-1'}`} />
                      </button>
                    </div>

                    {/* Checklist de Requisitos */}
                    <div className="space-y-3 pt-4 border-t border-slate-200">
                      <p className="text-[10px] font-black text-[#95A5A6] uppercase tracking-widest mb-1">Requisitos del Sistema</p>

                      <div className="flex items-center justify-between text-sm font-bold">
                        <span className="text-slate-600">NIF configurado</span>
                        {tieneNif ? <CheckCircle2 className="text-green-500 w-5 h-5"/> : <XCircle className="text-red-400 w-5 h-5"/>}
                      </div>

                      <div className="flex items-center justify-between text-sm font-bold">
                        <span className="text-slate-600">Certificado Digital (.p12)</span>
                        {tieneCertificado ? <CheckCircle2 className="text-green-500 w-5 h-5"/> : <XCircle className="text-red-400 w-5 h-5"/>}
                      </div>
                    </div>

                    {!puedeActivarVerifactu && !datos.verifactuActivado && (
                      <div className="mt-4 flex items-start bg-red-50 p-3 rounded-lg border border-red-100">
                        <AlertTriangle size={16} className="text-red-500 mt-0.5 mr-2 shrink-0" />
                        <p className="text-xs font-bold text-red-700">
                          Debes completar los requisitos anteriores para activar VeriFactu.
                        </p>
                      </div>
                    )}
                  </div>

                  {/* Botón real de carga de Certificado */}
                  <label htmlFor="subir-cert-input" className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3 hover:bg-gray-50 uppercase tracking-widest transition-all cursor-pointer">
                    <Upload size={18} />
                    {certificadoFile || datos.rutaCertificado ? 'CERTIFICADO CARGADO' : 'CARGAR CERTIFICADO DIGITAL (.p12)'}
                    <input
                      id="subir-cert-input"
                      type="file"
                      accept=".p12,.pfx"
                      className="hidden"
                      onChange={handleCertificadoChange}
                    />
                  </label>

                  <button
                    onClick={() => setVista('ticket')}
                    className="w-full h-14 bg-gray-100 text-[#001D3D] font-black rounded-2xl hover:bg-gray-200 transition-all uppercase text-xs tracking-widest flex items-center justify-center mt-2"
                  >
                    Configurar Ticket <ChevronRight size={18} className="ml-2" />
                  </button>
                </div>
              ) : (
                /* --- VISTA DISEÑO TICKET --- */
                <div className="flex gap-8 animate-in slide-in-from-right-4 duration-300">
                  <div className="flex-1 space-y-4">
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Nombre Comercial</label>
                      <input
                        className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50]"
                        value={datos.nombreEmpresa}
                        onChange={e => setDatos({...datos, nombreEmpresa: e.target.value})}
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Pie de página del ticket</label>
                      <textarea
                        className="w-full h-32 p-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] resize-none"
                        value={datos.mensajeTicket}
                        onChange={e => setDatos({...datos, mensajeTicket: e.target.value})}
                      />
                    </div>

                    <label htmlFor="subir-logo-input" className="w-full h-14 border-2 border-dashed border-[#E0E6ED] rounded-2xl text-[#34495E] font-black text-xs flex items-center justify-center gap-3 cursor-pointer hover:bg-gray-50 transition-colors">
                      <Upload size={18} /> {logoFile ? 'LOGO SELECCIONADO' : 'CARGAR LOGO'}
                      <input
                        id="subir-logo-input"
                        type="file"
                        accept="image/png, image/jpeg"
                        className="hidden"
                        onChange={handleLogoChange}
                      />
                    </label>

                    <button onClick={() => setVista('fiscal')} className="text-[#00796B] font-black text-[10px] uppercase tracking-widest hover:underline mt-2">
                      Volver a datos fiscales
                    </button>
                  </div>

                  {/* Preview Real del Ticket */}
                  <div className="w-64 bg-white border-t-8 border-[#001D3D] p-5 shadow-xl text-[10px] font-mono text-[#2C3E50] flex flex-col items-center">
                    {(logoPreview || datos.rutaLogo) && (
                      <img
                        src={logoPreview || `http://localhost:8080/uploads/${datos.rutaLogo}`}
                        alt="Logo Ticket"
                        className="w-16 h-16 object-contain mb-3 grayscale contrast-125"
                      />
                    )}
                    <div className="text-center mb-6 w-full">
                      <p className="font-black text-xs mb-1 uppercase tracking-tighter">{datos.nombreEmpresa || 'NOMBRE DE LA TIENDA'}</p>
                      <p className="opacity-60">CIF: {datos.cif || '---------'}</p>
                      <p className="opacity-60 uppercase">{datos.direccion || 'DIRECCIÓN'}</p>
                    </div>
                    <div className="border-t border-dashed border-gray-300 my-3 pt-3 w-full">
                      <div className="flex justify-between font-black mb-2 uppercase">
                        <span>CANT. CONCEPTO</span>
                        <span>IMPORTE</span>
                      </div>
                      <div className="flex justify-between opacity-70">
                        <span>1x Producto Demo</span>
                        <span>10,00 €</span>
                      </div>
                    </div>
                    <div className="border-t-2 border-black my-3 pt-3 flex justify-between font-black text-sm w-full">
                      <span>TOTAL</span>
                      <span>10,00 €</span>
                    </div>
                    <div className="text-center mt-6 space-y-3 opacity-60 w-full">
                      <p className="font-bold italic">"{datos.mensajeTicket || 'Gracias por su visita'}"</p>
                      <div className="w-12 h-12 bg-gray-100 mx-auto flex items-center justify-center text-[8px] border border-gray-200 uppercase">QR</div>
                      <p className="text-[7px] tracking-[4px] font-black">SISTEMA VERIFACTU</p>
                    </div>
                  </div>
                </div>
              )}

              <button
                onClick={handleGuardar}
                disabled={loading}
                className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl mt-10 hover:bg-[#004D40] active:scale-[0.98] transition-all flex items-center justify-center gap-3 shadow-lg shadow-[#00796B]/20"
              >
                {loading ? <Loader2 className="animate-spin" /> : <><Check size={20} /> GUARDAR CONFIGURACIÓN</>}
              </button>
            </>
          ) : (
            /* --- VISTA DE ÉXITO  --- */
            <div className="py-10 flex flex-col items-center justify-center text-center animate-in zoom-in">
              <div className="w-20 h-20 bg-green-100 text-green-500 rounded-full flex items-center justify-center mb-6">
                <CheckCircle2 size={40} />
              </div>
              <h3 className="text-2xl font-black text-slate-800 mb-2">¡Configuración Guardada!</h3>
              <p className="text-slate-500 font-medium mb-8">
                Los datos del negocio y del ticket se han actualizado correctamente en el sistema.
              </p>
              <button
                onClick={resetModal}
                className="w-full h-14 bg-[#001D3D] text-white font-black rounded-2xl hover:bg-[#001226] transition-all flex items-center justify-center uppercase text-xs tracking-widest shadow-lg"
              >
                Cerrar y Volver
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}