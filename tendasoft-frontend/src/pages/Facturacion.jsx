import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  Search, FileText, ChevronDown, ChevronUp,
  CheckCircle2, XCircle, AlertCircle,
  BarChart3, ArrowLeft, Lock
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Facturacion() {
  const navigate = useNavigate();

  // --- SEGURIDAD: CONTROL DE ACCESO ---
  const [autorizado, setAutorizado] = useState(false);

  // --- ESTADOS DE DATOS ---
  const [modo, setModo] = useState('dia');
  const [tickets, setTickets] = useState([]);
  const [resumen, setResumen] = useState(null);
  const [loading, setLoading] = useState(false);
  const [expandedId, setExpandedId] = useState(null);

  // Filtros temporales
  const [dia, setDia] = useState(new Date().getDate());
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  const [año, setAño] = useState(2026);

  useEffect(() => {
    // 1. Verificamos quién intenta entrar
    const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));

    // Si no es ADMIN, lo echamos de aquí
    if (!auth || auth.rol !== 'ADMIN') {
      alert("Acceso denegado: Solo el administrador puede ver reportes financieros.");
      navigate('/dashboard');
    } else {
      setAutorizado(true);
    }
  }, [navigate]);

  // --- LÓGICA DE CARGA ---
  const cargarReporte = async (tipo) => {
      setLoading(true);
      try {
        const params = {
          año: año,
          mes: modo === 'año' ? null : mes,
          dia: modo === 'dia' ? dia : null
        };

        if (tipo === 'resumen') {
          // --- LLAMADA AL NUEVO DTO DE ESTADÍSTICAS ---
          const res = await axios.get('http://localhost:8080/api/ventas/estadisticas', { params });
          const data = res.data; // Aquí recibimos el EstadisticasDTO

          setResumen({
            total: data.totalFacturado || 0,
            count: data.totalTickets || 0,
            medio: data.ticketMedio || 0,
            // Mapeamos el Map del backend a nuestras variables
            efectivo: data.ventasPorMetodoPago?.EFECTIVO || 0,
            tarjeta: data.ventasPorMetodoPago?.TARJETA || 0,
            otros: data.ventasPorMetodoPago?.OTROS || 0
          });
          setTickets([]);
        } else {
          // --- LLAMADA AL LISTADO NORMAL ---
          const res = await axios.get('http://localhost:8080/api/ventas/periodo', { params });
          setTickets(res.data);
          setResumen(null);
        }
      } catch (e) {
        console.error("Error en facturación", e);
        alert("Error al obtener los datos del servidor");
      } finally {
        setLoading(false);
      }
    };

  // Si no está autorizado, no mostramos nada mientras redirige
  if (!autorizado) return null;

  return (
    <div className="h-screen bg-[#F4F7F9] p-6 flex flex-col font-sans overflow-hidden antialiased">

      {/* Cabecera con Badge de Admin */}
      <div className="flex items-center justify-between mb-6 flex-shrink-0">
        <div className="flex items-center">
          <h1 className="text-[#2C3E50] text-3xl font-bold tracking-tight mr-4">Facturación y Reportes</h1>
        </div>
        <button
          onClick={() => navigate('/dashboard')}
          className="flex items-center text-[#2C3E50] bg-white px-4 py-2 rounded-lg font-bold shadow-sm hover:bg-slate-50 transition-colors"
        >
          <ArrowLeft size={20} className="mr-2" /> Volver al Inicio
        </button>
      </div>

      <div className="flex-1 flex gap-6 overflow-hidden">

        {/* COLUMNA IZQUIERDA (45%): Filtros */}
        <div className="w-[45%] h-full">
          <div className="bg-white rounded-[20px] shadow-sm p-8 flex flex-col h-full border-none">
            <h2 className="text-[#34495E] font-bold text-xl mb-6">Filtro de búsqueda</h2>

            <div className="flex space-x-8 mb-8">
              {['dia', 'mes', 'año'].map((m) => (
                <label key={m} className="flex items-center space-x-3 cursor-pointer group">
                  <input
                    type="radio"
                    name="modo"
                    checked={modo === m}
                    onChange={() => setModo(m)}
                    className="w-5 h-5 text-[#00796B] focus:ring-[#00796B] border-gray-300"
                  />
                  <span className={`capitalize text-lg font-bold ${modo === m ? 'text-[#2C3E50]' : 'text-[#95A5A6]'}`}>
                    {m === 'año' ? 'Año' : m}
                  </span>
                </label>
              ))}
            </div>

            <div className="flex space-x-3 mb-10">
              {modo === 'dia' && (
                <select value={dia} onChange={(e) => setDia(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none text-[#2C3E50] font-black uppercase text-sm transition-all">
                  {[...Array(31)].map((_, i) => <option key={i+1} value={i+1}>Día {i+1}</option>)}
                </select>
              )}
              {modo !== 'año' && (
                <select value={mes} onChange={(e) => setMes(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none text-[#2C3E50] font-black uppercase text-sm transition-all">
                  {['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'].map((m, i) => (
                    <option key={i} value={i+1}>{m}</option>
                  ))}
                </select>
              )}
              <select value={año} onChange={(e) => setAño(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none text-[#2C3E50] font-black uppercase text-sm transition-all">
                {[2024, 2025, 2026].map(y => <option key={y} value={y}>{y}</option>)}
              </select>
            </div>

            <div className="mt-auto space-y-4">
              <button
                onClick={() => cargarReporte('resumen')}
                className="w-full h-[64px] bg-[#00796B] text-white font-black rounded-xl flex items-center justify-center space-x-3 shadow-lg hover:bg-[#004D40] active:scale-95 transition-all tracking-widest"
              >
                <BarChart3 size={24} /> <span>GENERAR RESUMEN</span>
              </button>

              <button
                onClick={() => cargarReporte('tickets')}
                className="w-full h-[64px] bg-white border-2 border-[#E0E6ED] text-[#34495E] font-black rounded-xl flex items-center justify-center space-x-3 hover:bg-gray-50 active:scale-95 transition-all tracking-widest"
              >
                <FileText size={24} /> <span>VER TICKETS</span>
              </button>
            </div>
          </div>
        </div>

        {/* COLUMNA DERECHA (55%): Lista de Facturas (Clon de la Imagen) */}
        <div className="w-[55%] h-full overflow-hidden flex flex-col">

          <div className="flex-1 overflow-y-auto pr-2 pb-6 scrollbar-hide">

            {/* Buscador superior */}
            {/* --- INICIO DEL BLOQUE CORREGIDO (Línea 180 aprox.) --- */}
            {tickets.length > 0 ? (
              <div className="space-y-4">
                {tickets.map((ticket) => (
                  <div key={ticket.idVenta} className="bg-white rounded-[24px] shadow-sm border-2 border-transparent hover:border-[#00796B]/20 overflow-hidden transition-all">
                    <div
                      onClick={() => setExpandedId(expandedId === ticket.idVenta ? null : ticket.idVenta)}
                      className="p-5 flex items-center justify-between cursor-pointer"
                    >
                      <div className="flex items-center space-x-5">
                        <div className="w-14 h-14 bg-[#E0F2F1] rounded-2xl flex items-center justify-center">
                          <FileText className="text-[#00695C]" size={28} />
                        </div>
                        <div>
                          <p className="font-black text-[#2C3E50] text-lg">{ticket.numeroFactura || "Factura XXXXX"}</p>
                          <p className="text-[12px] text-[#95A5A6] font-black">{new Date(ticket.fecha).toLocaleDateString()}</p>
                        </div>
                      </div>

                      <div className="flex items-center space-x-6">
                        <p className="font-black text-[#2C3E50] text-xl">{(ticket.total || 0).toFixed(2)} €</p>

                        <div className={`flex items-center space-x-2 px-4 py-2 rounded-full border-2 ${
                          ticket.estadoVerifactu === 'PENDIENTE_ENVIO' ? 'bg-orange-50 border-orange-100 text-orange-600' :
                          ticket.estadoVerifactu === 'ERROR' ? 'bg-red-50 border-red-100 text-red-600' :
                          'bg-green-50 border-green-100 text-green-600'
                        }`}>
                          {ticket.estadoVerifactu === 'PENDIENTE_ENVIO' ? <AlertCircle size={16} /> :
                           ticket.estadoVerifactu === 'ERROR' ? <XCircle size={16} /> : <CheckCircle2 size={16} />}
                          <span className="text-[11px] font-black uppercase tracking-tighter">VeriFactu</span>
                        </div>
                        {expandedId === ticket.idVenta ? <ChevronUp className="text-[#95A5A6]" /> : <ChevronDown className="text-[#95A5A6]" />}
                      </div>
                    </div>

                    {/* Acordeón abierto - Detalle de productos REALES */}
                    {expandedId === ticket.idVenta && (
                      <div className="px-24 pb-8 pt-4 bg-slate-50/50 animate-in slide-in-from-top-4 duration-300">
                         <div className="grid grid-cols-3 text-[11px] font-black text-[#95A5A6] mb-3 uppercase tracking-widest border-b border-gray-200 pb-2">
                           <div>Concepto</div>
                           <div className="text-center">Cant.</div>
                           <div className="text-right">Subtotal</div>
                         </div>
                         <div className="space-y-3 mb-6">
                            {ticket.lineas && ticket.lineas.map((linea, idx) => (
                              <div key={idx} className="grid grid-cols-3 text-[14px] font-bold text-[#2C3E50]">
                                <div>{linea.nombreProducto}</div>
                                <div className="text-center text-[#00796B]">x{linea.cantidad}</div>
                                <div className="text-right">{(Number(linea.precioUnitario || 0) * linea.cantidad).toFixed(2)} €</div>
                              </div>
                            ))}
                         </div>
                         <div className="flex justify-between items-center p-4 bg-white rounded-xl border border-gray-100 shadow-sm">
                           <p className="text-[12px] text-[#7F8C8D] font-bold">Método de pago: <span className="text-[#00796B] font-black uppercase ml-1">{ticket.metodoPago}</span></p>
                           <p className="text-[12px] text-[#7F8C8D] font-bold">Cajero: <span className="text-[#2C3E50] ml-1">{ticket.nombreCajero}</span></p>
                         </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : resumen ? (
              /* Panel de Resumen Financiero */
              <div className="bg-white rounded-[24px] shadow-sm p-10 animate-in zoom-in-95">
                <h2 className="text-[#34495E] text-2xl font-black mb-1">Resumen Financiero</h2>
                <p className="text-[#95A5A6] font-bold mb-10 uppercase tracking-widest text-xs">Periodo: {modo} {mes}/{año}</p>

                <div className="bg-[#F8F9FA] rounded-[24px] p-10 text-center mb-10 border-2 border-dashed border-gray-200">
                  <p className="text-[#7F8C8D] text-[13px] font-black tracking-[0.3em] uppercase mb-3">TOTAL FACTURADO (PVP)</p>
                  <p className="text-[#00796B] text-6xl font-black">{(resumen.total || 0).toFixed(2)} €</p>
                </div>

                <div className="grid grid-cols-2 gap-10 mb-12 px-4">
                   <div className="border-l-4 border-[#2ECC71] pl-6">
                      <p className="text-[#7F8C8D] text-[11px] font-black uppercase mb-1">Cobros Efectivo</p>
                      <p className="text-[#2C3E50] text-2xl font-black">{(resumen.efectivo || 0).toFixed(2)} €</p>
                   </div>
                   <div className="border-l-4 border-[#1976D2] pl-6">
                      <p className="text-[#7F8C8D] text-[11px] font-black uppercase mb-1">Cobros Tarjeta</p>
                      <p className="text-[#2C3E50] text-2xl font-black">{(resumen.tarjeta || 0).toFixed(2)} €</p>
                   </div>
                </div>

                <div className="bg-[#E0F2F1] rounded-[20px] p-8 flex justify-between items-center shadow-inner">
                   <div className="text-center flex-1">
                      <p className="text-[#00695C] text-[11px] font-black uppercase mb-1">Operaciones</p>
                      <p className="text-[#004D40] text-2xl font-black">{resumen.count}</p>
                   </div>
                   <div className="w-[2px] h-12 bg-[#B2DFDB]" />
                   <div className="text-center flex-1">
                      <p className="text-[#00695C] text-[11px] font-black uppercase mb-1">Ticket Promedio</p>
                      <p className="text-[#004D40] text-2xl font-black">{(resumen.medio || 0).toFixed(2)} €</p>
                   </div>
                </div>
              </div>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-[#95A5A6] bg-white rounded-[24px] border-2 border-dashed border-gray-100">
                <BarChart3 size={64} className="mb-4 opacity-10" />
                <p className="font-black uppercase tracking-widest text-sm opacity-40">Selecciona filtros para ver datos</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}