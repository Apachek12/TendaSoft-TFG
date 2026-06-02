import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  FileText, ChevronDown, ChevronUp,
  CheckCircle2, Clock, BarChart3, ArrowLeft, RotateCcw, XCircle
} from 'lucide-react';
import { toast, Toaster } from 'react-hot-toast';

const API = import.meta.env.VITE_API_URL;
const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
const AÑOS  = [2024, 2025, 2026];

export default function Facturacion() {
  const navigate = useNavigate();

  const [autorizado, setAutorizado] = useState(false);
  const [modo, setModo]             = useState('dia');
  const [tickets, setTickets]       = useState([]);
  const [resumen, setResumen]       = useState(null);
  const [loading, setLoading]       = useState(false);
  const [expandedId, setExpandedId] = useState(null);
  const [dia, setDia] = useState(new Date().getDate());
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  const [año, setAño] = useState(new Date().getFullYear());

  useEffect(() => {
    const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
    if (!auth || auth.rol !== 'ADMIN') { navigate('/dashboard'); return; }
    setAutorizado(true);
  }, [navigate]);

  const cargarReporte = async (tipo) => {
    setLoading(true);
    const params = {
      año,
      mes: modo === 'año' ? undefined : mes,
      dia: modo === 'dia' ? dia : undefined
    };

    try {
      if (tipo === 'resumen') {
        const { data } = await axios.get(`${API}/api/ventas/estadisticas`, { params });
        setResumen({
          total:    data.totalFacturado || 0,
          count:    data.totalTickets   || 0,
          medio:    data.ticketMedio    || 0,
          efectivo: data.ventasPorMetodoPago?.EFECTIVO || 0,
          tarjeta:  data.ventasPorMetodoPago?.TARJETA  || 0,
          otros:    data.ventasPorMetodoPago?.OTROS    || 0,
          productosVendidos: data.productosVendidos    || 0,
          topProductos: data.productosMasVendidos      || {}
        });
        setTickets([]);
        toast.success('Resumen generado');
      } else {
        const { data } = await axios.get(`${API}/api/ventas/periodo`, { params });
        setTickets(data);
        setResumen(null);
        if (data.length === 0) toast.error('Sin registros en este periodo');
      }
    } catch {
      toast.error('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  const handleReintentarEnvio = async (idVenta) => {
    const tId = toast.loading('Procesando reintento VeriFactu...');
    try {
      await axios.post(`${API}/api/ventas/reintentar/${idVenta}`);
      setTickets(prev => prev.map(t =>
        t.idVenta === idVenta ? { ...t, estadoVerifactu: 'CORRECTO' } : t
      ));
      toast.success('Factura informada correctamente a la AEAT', { id: tId });
    } catch (e) {
      toast.error('Fallo en la comunicación: ' + (e.response?.data || 'Servidor offline'), { id: tId });
    }
  };

  // Clasifica el estado VeriFactu en categorías visuales
  const clasificarEstado = (estadoRaw) => {
    const est = (estadoRaw || '').toUpperCase().trim();
    if (est === 'CORRECTO') return {
      esCorrecta: true, esFirmadaLocal: false, esError: false,
      etiqueta: 'AEAT Informada', clases: 'bg-green-50 border-green-200 text-green-600', icono: 'check'
    };
    if (est === 'FIRMADO' || est === 'FIRMADO_Y_PENDIENTE_ENVIO') return {
      esCorrecta: false, esFirmadaLocal: true, esError: false,
      etiqueta: 'Firmada Local', clases: 'bg-blue-50 border-blue-200 text-blue-600', icono: 'firmada'
    };
    if (est.startsWith('ERROR')) return {
      esCorrecta: false, esFirmadaLocal: false, esError: true,
      etiqueta: est === 'ERROR_AEAT' ? 'Error AEAT' : est === 'ERROR_FIRMA' ? 'Error Firma' : 'Error XML',
      clases: 'bg-red-50 border-red-200 text-red-600', icono: 'error'
    };
    return {
      esCorrecta: false, esFirmadaLocal: false, esError: false,
      etiqueta: 'Pendiente', clases: 'bg-amber-50 border-amber-200 text-amber-600', icono: 'clock'
    };
  };

  if (!autorizado) return null;

  return (
    <div className="h-screen bg-[#F4F7F9] p-6 flex flex-col font-sans overflow-hidden antialiased">
      <Toaster position="top-right" />

      {/* Cabecera */}
      <div className="flex items-center justify-between mb-8 flex-shrink-0">
        <div className="flex items-center">
          <button onClick={() => navigate('/dashboard')} className="mr-4 p-2 text-slate-500 hover:bg-slate-200 rounded-full transition-all">
            <ArrowLeft size={24} />
          </button>
          <div className="flex items-center text-[#2C3E50] border-l pl-4 border-slate-200">
            <FileText className="mr-4 text-[#2C3E50]" size={32} />
            <div>
              <h1 className="text-3xl font-bold tracking-tight leading-none">Facturación y Reportes</h1>
              <p className="text-[#7F8C8D] text-sm mt-1 font-medium">Gestión de estados VeriFactu y auditoría</p>
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 flex gap-6 overflow-hidden">
        <div className="w-[45%] h-full">
          <div className="bg-white rounded-[20px] shadow-sm p-8 flex flex-col h-full">
            <h2 className="text-[#34495E] font-bold text-xl mb-6">Filtro de búsqueda</h2>

            <div className="flex space-x-8 mb-8">
              {['dia', 'mes', 'año'].map(m => (
                <label key={m} className="flex items-center space-x-3 cursor-pointer">
                  <input type="radio" name="modo" checked={modo === m} onChange={() => setModo(m)} className="w-5 h-5 text-[#00796B] focus:ring-[#00796B]" />
                  <span className={`capitalize text-lg font-bold ${modo === m ? 'text-[#2C3E50]' : 'text-[#95A5A6]'}`}>{m}</span>
                </label>
              ))}
            </div>

            <div className="flex space-x-3 mb-10">
              {modo === 'dia' && (
                <select value={dia} onChange={e => setDia(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none font-black text-sm transition-all">
                  {[...Array(31)].map((_, i) => <option key={i+1} value={i+1}>Día {i+1}</option>)}
                </select>
              )}
              {modo !== 'año' && (
                <select value={mes} onChange={e => setMes(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none font-black text-sm transition-all">
                  {MESES.map((m, i) => <option key={i+1} value={i+1}>{m}</option>)}
                </select>
              )}
              <select value={año} onChange={e => setAño(e.target.value)} className="flex-1 h-14 bg-[#F8F9FA] border-2 border-transparent focus:border-[#00796B] rounded-xl px-4 outline-none font-black text-sm transition-all">
                {AÑOS.map(y => <option key={y} value={y}>{y}</option>)}
              </select>
            </div>

            <div className="mt-auto space-y-4">
              <button onClick={() => cargarReporte('resumen')} className="w-full h-[64px] bg-[#00796B] text-white font-black rounded-xl shadow-lg hover:bg-[#004D40] transition-all tracking-widest flex items-center justify-center space-x-3">
                <BarChart3 size={24} /><span>GENERAR RESUMEN</span>
              </button>
              <button onClick={() => cargarReporte('tickets')} className="w-full h-[64px] bg-white border-2 border-[#E0E6ED] text-[#34495E] font-black rounded-xl hover:bg-gray-50 transition-all tracking-widest flex items-center justify-center space-x-3">
                <FileText size={24} /><span>VER TICKETS</span>
              </button>
            </div>
          </div>
        </div>
        <div className="w-[55%] h-full overflow-hidden flex flex-col">
          <div className="flex-1 overflow-y-auto pr-2 pb-6 scrollbar-hide">

            {tickets.length > 0 ? (
              <div className="space-y-4">
                {tickets.map(ticket => {
                  const estado   = clasificarEstado(ticket.estadoVerifactu);
                  const expandido = expandedId === ticket.idVenta;
                  return (
                    <div key={ticket.idVenta} className="bg-white rounded-[24px] shadow-sm border-2 border-transparent hover:border-[#00796B]/20 overflow-hidden transition-all">
                      <div onClick={() => setExpandedId(expandido ? null : ticket.idVenta)} className="p-5 flex items-center justify-between cursor-pointer">
                        <div className="flex items-center space-x-5">
                          <div className="w-14 h-14 bg-[#E0F2F1] rounded-2xl flex items-center justify-center">
                            <FileText className="text-[#00695C]" size={28} />
                          </div>
                          <div>
                            <p className="font-black text-[#2C3E50] text-lg">{ticket.numeroFactura}</p>
                            <p className="text-[12px] text-[#95A5A6] font-black">{new Date(ticket.fecha).toLocaleDateString()}</p>
                          </div>
                        </div>

                        <div className="flex items-center space-x-4">
                          <p className="font-black text-[#2C3E50] text-xl">{(ticket.total || 0).toFixed(2)} €</p>
                          <div className="flex items-center space-x-2">
                            <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border shadow-sm ${estado.clases}`}>
                              {estado.icono === 'check'   && <CheckCircle2 size={14} />}
                              {estado.icono === 'firmada' && <FileText size={14} className="animate-pulse" />}
                              {estado.icono === 'error'   && <XCircle size={14} />}
                              {estado.icono === 'clock'   && <Clock size={14} />}
                              <span className="text-[10px] font-black uppercase tracking-widest">{estado.etiqueta}</span>
                            </div>
                            {!estado.esCorrecta && (
                              <button
                                onClick={e => { e.stopPropagation(); handleReintentarEnvio(ticket.idVenta); }}
                                title={estado.esFirmadaLocal ? 'Enviar a la AEAT ahora' : 'Reintentar firma y envío'}
                                className="p-2 bg-slate-50 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-full border border-slate-200 transition-all group"
                              >
                                <RotateCcw size={14} className="group-hover:rotate-180 transition-transform duration-500" />
                              </button>
                            )}
                          </div>
                          {expandido ? <ChevronUp className="text-[#95A5A6]" /> : <ChevronDown className="text-[#95A5A6]" />}
                        </div>
                      </div>

                      {expandido && (
                        <div className="px-24 pb-8 pt-4 bg-slate-50/50 animate-in slide-in-from-top-4 duration-300">
                          <div className="grid grid-cols-3 text-[11px] font-black text-[#95A5A6] border-b border-gray-200 pb-2 mb-3 uppercase tracking-widest">
                            <div>Concepto</div><div className="text-center">Cant.</div><div className="text-right">Subtotal</div>
                          </div>
                          <div className="space-y-2">
                            {ticket.lineas?.map((l, idx) => (
                              <div key={idx} className="grid grid-cols-3 text-[14px] font-bold text-[#2C3E50]">
                                <div>{l.nombreProducto}</div>
                                <div className="text-center text-[#00796B]">x{l.cantidad}</div>
                                <div className="text-right">{(l.precioUnitario * l.cantidad).toFixed(2)} €</div>
                              </div>
                            ))}
                          </div>
                          <div className="flex justify-between items-center p-4 bg-white rounded-xl mt-4 border border-gray-100 shadow-sm">
                            <p className="text-[12px] font-bold text-slate-500 uppercase">Método: <span className="text-[#00796B] font-black">{ticket.metodoPago}</span></p>
                            <p className="text-[12px] font-bold text-slate-500 uppercase">Cajero: <span className="text-[#2C3E50] font-black">{ticket.nombreCajero}</span></p>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

            ) : resumen ? (
              <div className="bg-white rounded-[24px] shadow-sm p-10 animate-in zoom-in-95 duration-300">
                <h2 className="text-[#34495E] text-2xl font-black mb-1">Resumen Financiero</h2>
                <p className="text-[#95A5A6] font-bold mb-8 uppercase tracking-widest text-xs">Filtro: {modo}</p>

                {/* Total Facturado */}
                <div className="bg-[#F8F9FA] rounded-[24px] p-8 text-center mb-6 border-2 border-dashed border-gray-200">
                  <p className="text-[#7F8C8D] text-[12px] font-black tracking-[0.3em] uppercase mb-2">TOTAL FACTURADO (PVP)</p>
                  <p className="text-[#00796B] text-5xl font-black">{(resumen.total || 0).toFixed(2)} €</p>
                </div>

                {/* Tickets, Media y Unidades Totales */}
                <div className="grid grid-cols-3 gap-4 mb-8 bg-slate-50 p-4 rounded-2xl border border-slate-100">
                  <div className="text-center border-r border-slate-200">
                    <p className="text-[#7F8C8D] text-[10px] font-black uppercase mb-1">Nº Tickets</p>
                    <p className="text-[#2C3E50] text-lg font-black">{resumen.count}</p>
                  </div>
                  <div className="text-center border-r border-slate-200">
                    <p className="text-[#7F8C8D] text-[10px] font-black uppercase mb-1">Ticket Medio</p>
                    <p className="text-[#2C3E50] text-lg font-black">{(resumen.medio || 0).toFixed(2)} €</p>
                  </div>
                  <div className="text-center">
                    <p className="text-[#7F8C8D] text-[10px] font-black uppercase mb-1">Uds. Vendidas</p>
                    <p className="text-[#00796B] text-lg font-black">{resumen.productosVendidos} uds</p>
                  </div>
                </div>

                {/* Productos Más Vendidos */}
                <div className="mb-8">
                  <p className="text-[#7F8C8D] text-[11px] font-black uppercase mb-3 tracking-wider">Productos más vendidos:</p>
                  {resumen.topProductos && Object.keys(resumen.topProductos).length > 0 ? (
                    <div className="space-y-2">
                      {Object.entries(resumen.topProductos).map(([producto, cantidad], index) => (
                        <div key={producto} className="flex items-center justify-between bg-gray-50 hover:bg-slate-100/80 px-4 py-3 rounded-xl border border-gray-100 transition-all">
                          <div className="flex items-center space-x-3">
                            <span className={`w-6 h-6 flex items-center justify-center rounded-full text-[10px] font-black ${
                              index === 0 ? 'bg-amber-100 text-amber-700' :
                              index === 1 ? 'bg-slate-200 text-slate-700' : 'bg-orange-100 text-orange-700'
                            }`}>
                              {index + 1}
                            </span>
                            <span className="text-sm font-bold text-[#2C3E50]">{producto}</span>
                          </div>
                          <span className="text-xs font-black bg-[#E0F2F1] text-[#00796B] px-2.5 py-1 rounded-lg">
                            {cantidad} uds
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-xs text-slate-400 italic pl-1">No hay registros de artículos en este periodo.</p>
                  )}
                </div>

                {/* Métodos de Pago */}
                <p className="text-[#7F8C8D] text-[11px] font-black uppercase mb-4 tracking-wider">Desglose por método de pago:</p>
                <div className="grid grid-cols-3 gap-6">
                  {[
                    { label: 'Efectivo', valor: resumen.efectivo, color: '#2ECC71' },
                    { label: 'Tarjeta',  valor: resumen.tarjeta,  color: '#1976D2' },
                    { label: 'Otros',    valor: resumen.otros,    color: '#95A5A6' }
                  ].map(({ label, valor, color }) => (
                    <div key={label} style={{ borderLeftColor: color }} className="border-l-4 pl-4">
                      <p className="text-[#7F8C8D] text-[10px] font-black uppercase mb-1">{label}</p>
                      <p className="text-[#2C3E50] text-xl font-black">{(valor || 0).toFixed(2)} €</p>
                    </div>
                  ))}
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