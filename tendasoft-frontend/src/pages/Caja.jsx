import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { QRCodeSVG } from 'qrcode.react';
import {
  Delete, ImageIcon, ChevronLeft, ChevronRight, Trash2,
  Banknote, CreditCard, ArrowLeft, Printer, Monitor, X, CheckCircle
} from 'lucide-react';
import { toast, Toaster } from 'react-hot-toast';

const API = 'http://localhost:8080';

// ── Modal Ticket ─────────────────────────────────────────────────────────────
function ModalTicket({ ventaGuardada, lineas, total, datosNegocio, onCerrar }) {
  const fecha = new Date();

  const urlVerifactu =
    `https://www2.agenciatributaria.gob.es/wlpl/inwinv/es/es.aeat.dit.adu.eaf.j.VerificaQrFacturaEAF` +
    `?nif=${datosNegocio?.cif || ''}&numserie=${ventaGuardada?.numeroFactura || ''}` +
    `&fecha=${fecha.toISOString().split('T')[0]}&importe=${total.toFixed(2)}`;

  const baseImpTotal = lineas.reduce((acc, item) => {
    const iva = item.porcentajeIva ?? 21;
    return acc + (item.precio * item.cantidad) / (1 + iva / 100);
  }, 0);
  const cuotaIva = total - baseImpTotal;

  return (
    <div
      className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={onCerrar}
    >
      <div
        className="bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden"
        onClick={e => e.stopPropagation()}
        style={{ fontFamily: "'Courier New', Courier, monospace" }}
      >
        {/* Cabecera */}
        <div className="bg-[#2ECC71] px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2 text-white">
            <CheckCircle size={22} />
            <span className="font-bold text-sm tracking-wide">VENTA REGISTRADA</span>
          </div>
          <button onClick={onCerrar} className="text-white/80 hover:text-white">
            <X size={20} />
          </button>
        </div>

        {/* Cuerpo */}
        <div className="px-6 py-4 max-h-[70vh] overflow-y-auto">

          {/* Empresa */}
          <div className="text-center mb-4 border-b border-dashed border-gray-300 pb-4">
            <p className="font-black text-xl text-[#2C3E50] tracking-tight">
              {datosNegocio?.nombreEmpresa || 'TIENDA'}
            </p>
            {datosNegocio?.cif && (
              <p className="text-xs text-gray-500 mt-0.5">CIF: {datosNegocio.cif}</p>
            )}
            {datosNegocio?.direccion && (
              <p className="text-xs text-gray-500">{datosNegocio.direccion}</p>
            )}
            <p className="text-xs text-gray-400 mt-1">
              {fecha.toLocaleDateString('es-ES')}{' '}
              {fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
            </p>
            {ventaGuardada?.numeroFactura && (
              <p className="text-xs text-gray-500 mt-0.5">Nº: {ventaGuardada.numeroFactura}</p>
            )}
          </div>

          {/* Líneas */}
          <div className="mb-4 border-b border-dashed border-gray-300 pb-4 space-y-1.5">
            {lineas.map((item, i) => (
              <div key={i} className="flex justify-between items-start text-sm">
                <div className="flex-1 pr-2">
                  <p className="font-bold text-[#2C3E50] leading-tight">{item.nombre}</p>
                  <p className="text-xs text-gray-400">
                    {item.cantidad} x {item.precio.toFixed(2)} €
                    {item.porcentajeIva != null && ` (IVA ${item.porcentajeIva}%)`}
                  </p>
                </div>
                <p className="font-black text-[#2C3E50] whitespace-nowrap">
                  {(item.precio * item.cantidad).toFixed(2)} €
                </p>
              </div>
            ))}
          </div>

          {/* Totales */}
          <div className="mb-4 border-b border-dashed border-gray-300 pb-4 space-y-1 text-sm">
            <div className="flex justify-between text-gray-500">
              <span>Base imponible</span>
              <span>{baseImpTotal.toFixed(2)} €</span>
            </div>
            <div className="flex justify-between text-gray-500">
              <span>IVA</span>
              <span>{cuotaIva.toFixed(2)} €</span>
            </div>
            <div className="flex justify-between font-black text-xl text-[#2C3E50] mt-1">
              <span>TOTAL</span>
              <span>{total.toFixed(2)} €</span>
            </div>
          </div>

          {/* QR VeriFactu */}
          <div className="flex flex-col items-center mb-4 border-b border-dashed border-gray-300 pb-4">
            <p className="text-[10px] text-gray-400 uppercase tracking-widest mb-2">
              Verificación VeriFactu
            </p>
            <QRCodeSVG value={urlVerifactu} size={128} />
            <p className="text-[9px] text-gray-300 mt-1 text-center">
              Escanea para verificar en la AEAT
            </p>
          </div>

          {/* Estado VeriFactu */}
          {ventaGuardada?.estadoVerifactu && (
            <div className="text-center mb-3">
              <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                ventaGuardada.estadoVerifactu.toUpperCase() === 'CORRECTO'
                  ? 'bg-green-100 text-green-700'
                  : 'bg-yellow-100 text-yellow-700'
              }`}>
                VeriFactu: {ventaGuardada.estadoVerifactu}
              </span>
            </div>
          )}

          {/* Mensaje pie */}
          <p className="text-center text-xs text-gray-400 italic">
            {datosNegocio?.mensajeTicket || '¡Gracias por su compra!'}
          </p>
        </div>

        {/* Botón cerrar */}
        <div className="px-6 pb-5 pt-2">
          <button
            onClick={onCerrar}
            className="w-full bg-[#2C3E50] text-white font-bold py-3 rounded-xl hover:bg-black transition-colors text-sm tracking-wide"
          >
            CERRAR TICKET
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Caja principal ────────────────────────────────────────────────────────────
export default function Caja() {
  const navigate = useNavigate();
  const scrollCategoriasRef = useRef(null);
  const [usuarioNombre, setUsuarioNombre]     = useState('Vendedor');
  const [productos, setProductos]             = useState([]);
  const [categorias, setCategorias]           = useState([]);
  const [catSeleccionada, setCatSeleccionada] = useState('todas');
  const [showProducts, setShowProducts]       = useState(false);
  const [ventaActual, setVentaActual]         = useState([]);
  const [codigoInput, setCodigoInput]         = useState('');
  const [pasoPago, setPasoPago]               = useState(null);
  const [dineroEntregado, setDineroEntregado] = useState('');
  const [cambio, setCambio]                   = useState(null);
  const [datosNegocio, setDatosNegocio]       = useState(null);
  const [ticketData, setTicketData]           = useState(null);

  const total = ventaActual.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  useEffect(() => {
    const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
    if (auth) setUsuarioNombre(auth.nombreUsuario || auth.nombreReal);
    fetchData();
    fetchDatosNegocio();
  }, []);

  const fetchDatosNegocio = async () => {
    try {
      const { data } = await axios.get(`${API}/api/datos-negocio`);
      setDatosNegocio(data);
    } catch (e) {
      console.warn('No se pudieron cargar datos del negocio');
    }
  };

  const fetchData = async () => {
    try {
      const [resProd, resCat] = await Promise.all([
        axios.get(`${API}/api/productos`),
        axios.get(`${API}/api/categorias`)
      ]);
      setCategorias(resCat.data);
      const cruzados = resCat.data.flatMap(cat =>
        (cat.productos || []).map(p => ({ codigoBarras: p.codigoBarras, idCategoriaReal: cat.id }))
      );
      const productosFinales = resProd.data
        .filter(p => p.activo !== false)
        .map(p => {
          const match = cruzados.find(c => c.codigoBarras === p.codigoBarras);
          return { ...p, idCategoriaReal: match ? match.idCategoriaReal : 'sin-categoria' };
        });
      setProductos(productosFinales);
    } catch (e) {
      console.error('Error cargando el catálogo:', e);
      toast.error('Error al cargar productos');
    }
  };

  const agregarProducto = (prod) => {
    setVentaActual(prev => {
      const existe = prev.find(item => item.codigoBarras === prod.codigoBarras);
      if (existe) {
        return prev.map(item =>
          item.codigoBarras === prod.codigoBarras
            ? { ...item, cantidad: item.cantidad + 1 }
            : item
        );
      }
      return [...prev, { ...prod, cantidad: 1 }];
    });
    setCodigoInput('');
  };

  const eliminarProductoDeVenta = (codigoBarras) => {
    setVentaActual(prev => prev.filter(item => item.codigoBarras !== codigoBarras));
  };

  const validarStockAntesDeCobrar = () => {
    for (const item of ventaActual) {
      const master = productos.find(p => p.codigoBarras === item.codigoBarras);
      if (master && item.cantidad > master.unidades) {
        toast.error(`Stock insuficiente: ${item.nombre}. Solo quedan ${master.unidades} unidades.`, {
          icon: '⚠️', duration: 4000
        });
        return false;
      }
    }
    return true;
  };

  const calcularCambio = () => {
    const entregado = parseFloat(dineroEntregado.replace(',', '.'));
    if (isNaN(entregado) || entregado < total) {
      toast.error('Cantidad entregada insuficiente o inválida');
      return;
    }
    setCambio(entregado - total);
  };

  const desplazarCategorias = (direccion) => {
    scrollCategoriasRef.current?.scrollBy({
      left: direccion === 'izq' ? -150 : 150,
      behavior: 'smooth'
    });
  };

  const handleNumpadClick = (n) => {
    if (pasoPago === 'efectivo') setDineroEntregado(prev => prev + n);
    else if (!pasoPago) setCodigoInput(prev => prev + n);
  };

  const handleNumpadDelete = () => {
    if (pasoPago === 'efectivo') setDineroEntregado(prev => prev.slice(0, -1));
    else if (!pasoPago) setCodigoInput(prev => prev.slice(0, -1));
  };

  const handleEnterClick = () => {
    if (pasoPago === 'efectivo') {
      calcularCambio();
    } else if (!pasoPago) {
      const encontrado = productos.find(p => p.codigoBarras === codigoInput);
      if (encontrado) agregarProducto(encontrado);
      else toast.error('Producto no encontrado', { duration: 2000 });
    }
  };

  const finalizarVenta = async (mostrarTicket) => {
    if (ventaActual.length === 0) return;
    const tId = toast.loading('Registrando venta...');
    try {
      const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft')) || {};
      const idUsuario = parseInt(auth.idUsuario || auth.id || 1);
      const payload = {
        venta: {
          usuario: { idUsuario },
          metodoPago: pasoPago.toUpperCase(),
          total: parseFloat(total.toFixed(2)),
          dineroEntregado: pasoPago === 'efectivo'
            ? parseFloat(String(dineroEntregado).replace(',', '.'))
            : parseFloat(total.toFixed(2)),
          cambio: pasoPago === 'efectivo' ? parseFloat(cambio.toFixed(2)) : 0
        },
        lineas: ventaActual.map(item => ({
          producto: { codigoBarras: String(item.codigoBarras) },
          cantidad: parseInt(item.cantidad, 10)
        }))
      };

      const { data: ventaGuardada } = await axios.post(`${API}/api/ventas`, payload);

      const estadoRaw = ventaGuardada.estadoVerifactu || ventaGuardada.estado_verifactu || '';
      const estVF     = String(estadoRaw).toUpperCase().trim();
      const esCorrecta       = estVF === 'CORRECTO';
      const esPendienteEnvio = estVF === 'FIRMADO' || estVF === 'FIRMADO_Y_PENDIENTE_ENVIO';

      if (esCorrecta || esPendienteEnvio) {
        toast.success(
          esCorrecta ? 'Venta completada y enviada a la AEAT' : 'Venta completada (envío AEAT pendiente)',
          { id: tId, duration: 3000 }
        );
      } else {
        toast.error(`Venta guardada, pero VeriFactu: ${estVF || 'Error'}`, { id: tId, duration: 4000 });
      }

      if (mostrarTicket) {
        setTicketData({
          ventaGuardada,
          lineas: ventaActual.map(item => ({
            nombre: item.nombre,
            cantidad: item.cantidad,
            precio: item.precio,
            porcentajeIva: item.porcentajeIva ?? null
          })),
          total
        });
      }

      setVentaActual([]);
      setPasoPago(null);
      setDineroEntregado('');
      setCambio(null);
      fetchData();
    } catch (error) {
      console.error('Error al registrar la venta:', error);
      toast.error(
        'Error al registrar venta: ' + (error?.response?.data?.message || 'Revisa la consola del servidor'),
        { id: tId }
      );
    }
  };

  return (
    <div className="h-screen bg-[#F4F7F9] p-4 flex flex-col font-sans overflow-hidden antialiased">
      <Toaster position="top-right" reverseOrder={false} />

      {ticketData && (
        <ModalTicket
          ventaGuardada={ticketData.ventaGuardada}
          lineas={ticketData.lineas}
          total={ticketData.total}
          datosNegocio={datosNegocio}
          onCerrar={() => setTicketData(null)}
        />
      )}

      {/* Cabecera */}
      <div className="flex items-center justify-between mb-4 flex-shrink-0">
        <div className="flex items-center">
          <button
            onClick={() => navigate('/dashboard')}
            className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
          >
            <ArrowLeft size={24} />
          </button>
          <div className="flex items-center text-[#2C3E50] border-l pl-4 border-slate-200">
            <Monitor className="mr-4 text-[#2C3E50]" size={32} />
            <div>
              <h1 className="text-2xl font-bold tracking-tight leading-none capitalize">Punto de Venta</h1>
              <p className="text-[#7F8C8D] text-xs mt-1 font-bold">
                Atendido por: <span className="text-[#1976D2]">{usuarioNombre}</span>
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Columna izquierda */}
        <div className="w-[60%] h-full pr-2">
          <div className="bg-white rounded-[16px] shadow-sm flex flex-col h-full overflow-hidden">
            {!pasoPago ? (
              <>
                <div className="flex bg-[#F8F9FA] px-4 py-2.5 text-[11px] font-bold text-[#7F8C8D] tracking-wider uppercase items-center">
                  <div className="flex-[2.5]">PRODUCTO</div>
                  <div className="flex-1 text-right">CANT.</div>
                  <div className="flex-[1.2] text-right">PRECIO</div>
                  <div className="w-8" />
                </div>
                <div className="flex-1 overflow-y-auto p-2 scrollbar-thin">
                  {ventaActual.length === 0 ? (
                    <div className="h-full flex items-center justify-center text-[#95A5A6] italic text-sm">
                      Esperando productos...
                    </div>
                  ) : (
                    ventaActual.map(item => {
                      const master      = productos.find(p => p.codigoBarras === item.codigoBarras);
                      const excedeStock = master && item.cantidad > master.unidades;
                      return (
                        <div
                          key={item.codigoBarras}
                          className={`flex px-4 py-3 border-b border-[#F4F7F9] items-center transition-colors ${excedeStock ? 'bg-red-50' : ''}`}
                        >
                          <div className="flex-[2.5]">
                            <p className={`font-bold text-sm ${excedeStock ? 'text-red-600' : 'text-[#2C3E50]'}`}>
                              {item.nombre}
                            </p>
                            {excedeStock && (
                              <p className="text-[9px] font-black text-red-500 uppercase italic">
                                ¡Solo hay {master.unidades} en stock!
                              </p>
                            )}
                            <p className="text-[10px] text-[#95A5A6]">{item.codigoBarras}</p>
                          </div>
                          <div className="flex-1 text-right font-bold text-[#2C3E50]">x{item.cantidad}</div>
                          <div className="flex-[1.2] text-right font-black text-[#2C3E50]">
                            {(item.precio * item.cantidad).toFixed(2)}€
                          </div>
                          <button
                            onClick={() => eliminarProductoDeVenta(item.codigoBarras)}
                            className="w-8 flex justify-end text-red-400 hover:text-red-600 ml-2"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      );
                    })
                  )}
                </div>
                <div className="h-[1px] bg-[#E0E6ED] w-full" />
                <div className="h-20 flex items-center px-6 justify-between bg-white">
                  <button
                    onClick={() => { if (validarStockAntesDeCobrar()) setPasoPago('seleccion'); }}
                    disabled={ventaActual.length === 0}
                    className="bg-[#2ECC71] text-white font-bold px-10 h-[56px] rounded-[12px] text-lg shadow-sm hover:bg-[#27ae60] active:scale-95 transition-all disabled:opacity-20"
                  >
                    COBRAR
                  </button>
                  <span className="text-[#2C3E50] text-[32px] font-black">{total.toFixed(2)} €</span>
                </div>
              </>
            ) : (
              <div className="flex flex-col h-full animate-in zoom-in-95 duration-200 bg-white relative">
                <button
                  onClick={() => { setPasoPago(null); setCambio(null); setDineroEntregado(''); }}
                  className="absolute top-4 left-4 p-2 flex items-center text-[#7F8C8D] hover:text-[#2C3E50] font-bold text-sm bg-slate-50 rounded-lg transition-colors"
                >
                  <ArrowLeft size={18} className="mr-1" /> Volver al ticket
                </button>
                {pasoPago === 'seleccion' && (
                  <div className="flex flex-col items-center justify-center h-full px-12">
                    <p className="text-[#1565C0] text-5xl font-black mb-10 text-center">TOTAL: {total.toFixed(2)} €</p>
                    <p className="text-[#7F8C8D] text-xl font-bold mb-6 uppercase tracking-wider">Método de Pago</p>
                    <div className="flex w-full space-x-6">
                      <button
                        onClick={() => setPasoPago('efectivo')}
                        className="flex-1 bg-[#2ECC71] text-white rounded-[16px] h-[100px] flex items-center justify-center text-2xl font-bold hover:bg-[#27ae60] shadow-md"
                      >
                        <Banknote className="mr-3" size={36} /> Efectivo
                      </button>
                      <button
                        onClick={() => setPasoPago('tarjeta')}
                        className="flex-1 bg-[#1976D2] text-white rounded-[16px] h-[100px] flex items-center justify-center text-2xl font-bold hover:bg-[#1565C0] shadow-md"
                      >
                        <CreditCard className="mr-3" size={36} /> Tarjeta
                      </button>
                    </div>
                  </div>
                )}
                {pasoPago === 'efectivo' && (
                  <div className="flex flex-col items-center justify-center h-full px-16 pt-8">
                    <p className="text-[#1565C0] text-4xl font-black mb-8">TOTAL: {total.toFixed(2)} €</p>
                    <div className="w-full text-center text-5xl font-black text-[#2C3E50] border-2 border-gray-200 rounded-[16px] py-6 mb-6 bg-slate-50">
                      {dineroEntregado ? `${dineroEntregado} €` : '0,00 €'}
                    </div>
                    {cambio !== null && (
                      <div className="animate-in slide-in-from-bottom-4 flex flex-col items-center w-full">
                        <p className="text-[#2ECC71] text-4xl font-black mb-8">Cambio: {cambio.toFixed(2)} €</p>
                        <BotonesFinalizacion onFinalizar={finalizarVenta} />
                      </div>
                    )}
                  </div>
                )}
                {pasoPago === 'tarjeta' && (
                  <div className="flex flex-col items-center justify-center h-full px-16">
                    <p className="text-[#1976D2] text-[64px] font-black mb-16 tracking-tighter">{total.toFixed(2)} €</p>
                    <BotonesFinalizacion onFinalizar={finalizarVenta} h="h-[80px]" />
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Columna derecha */}
        <div className="w-[40%] h-full flex flex-col">
          {!pasoPago && (
            <div className="animate-in fade-in">
              <input
                type="text"
                placeholder="Código de barras"
                value={codigoInput}
                onChange={(e) => setCodigoInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleEnterClick()}
                className="w-full h-[48px] bg-white border border-gray-200 rounded-[8px] text-center text-[16px] font-bold text-[#2C3E50] focus:outline-none mb-2 shadow-sm"
              />
              <button
                onClick={() => setShowProducts(!showProducts)}
                className="w-full h-[44px] bg-[#2C3E50] text-white font-bold rounded-[8px] uppercase text-xs tracking-widest mb-3 hover:bg-black transition-colors"
              >
                {showProducts ? 'OCULTAR CATÁLOGO' : 'VER CATÁLOGO'}
              </button>
            </div>
          )}
          <div className="flex-1 flex flex-col overflow-hidden">
            {showProducts && !pasoPago ? (
              <div className="flex flex-col h-full animate-in fade-in duration-200">
                <div className="flex items-center space-x-1 mb-3 w-full">
                  <button onClick={() => desplazarCategorias('izq')} className="p-1 text-slate-500 hover:text-black">
                    <ChevronLeft size={24} />
                  </button>
                  <div ref={scrollCategoriasRef} className="flex overflow-x-auto space-x-2 flex-1 scrollbar-hide pb-1">
                    <button
                      onClick={() => setCatSeleccionada('todas')}
                      className={`px-4 py-2 rounded-[6px] text-[12px] font-bold whitespace-nowrap transition-colors ${
                        catSeleccionada === 'todas'
                          ? 'bg-[#2C3E50] text-white'
                          : 'bg-white text-[#2C3E50] hover:bg-slate-100 border border-slate-200'
                      }`}
                    >
                      Todos
                    </button>
                    {categorias.map(cat => (
                      <button
                        key={cat.id}
                        onClick={() => setCatSeleccionada(cat.id)}
                        className={`px-4 py-2 rounded-[6px] text-[12px] font-bold whitespace-nowrap transition-colors ${
                          String(catSeleccionada) === String(cat.id)
                            ? 'bg-[#2C3E50] text-white'
                            : 'bg-white text-[#2C3E50] hover:bg-slate-100 border border-slate-200'
                        }`}
                      >
                        {cat.nombre}
                      </button>
                    ))}
                  </div>
                  <button onClick={() => desplazarCategorias('der')} className="p-1 text-slate-500 hover:text-black">
                    <ChevronRight size={24} />
                  </button>
                </div>
                <div className="flex-1 overflow-y-auto grid grid-cols-3 gap-3 content-start pb-2 pr-1 scrollbar-thin">
                  {productos
                    .filter(p => catSeleccionada === 'todas' || String(p.idCategoriaReal) === String(catSeleccionada))
                    .map(p => (
                      <div
                        key={p.codigoBarras}
                        onClick={() => agregarProducto(p)}
                        className="bg-white rounded-[12px] p-2 flex flex-col items-center text-center cursor-pointer active:scale-95 shadow-sm hover:shadow-md border border-slate-100 transition-all"
                      >
                        <div className="w-16 h-16 mb-2 rounded-lg overflow-hidden bg-slate-50 flex items-center justify-center shrink-0">
                          {p.urlImagen ? (
                            <img
                              src={`${API}/uploads/${p.urlImagen}`}
                              alt={p.nombre}
                              className="w-full h-full object-cover"
                              onError={(e) => {
                                e.target.style.display = 'none';
                                e.target.nextSibling.style.display = 'block';
                              }}
                            />
                          ) : null}
                          <ImageIcon size={24} className="text-slate-300" style={{ display: p.urlImagen ? 'none' : 'block' }} />
                        </div>
                        <p className="text-[11px] font-bold text-[#2C3E50] truncate w-full leading-tight">{p.nombre}</p>
                        <p className="text-[14px] font-black text-[#1976D2] mt-1">{p.precio.toFixed(2)} €</p>
                      </div>
                    ))
                  }
                </div>
              </div>
            ) : (
              <div className={`grid grid-cols-3 gap-1.5 flex-1 pb-2 ${pasoPago ? 'pt-2' : ''}`}>
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, ',', 0].map(n => (
                  <button
                    key={n}
                    onClick={() => handleNumpadClick(n)}
                    className="bg-white rounded-[12px] text-[28px] font-bold text-[#2C3E50] active:bg-slate-200 shadow-sm border border-gray-100 transition-colors"
                  >
                    {n}
                  </button>
                ))}
                <button
                  onClick={handleNumpadDelete}
                  className="bg-white rounded-[12px] flex items-center justify-center border border-gray-100 active:bg-slate-200 transition-colors shadow-sm"
                >
                  <Delete size={32} className="text-[#95A5A6]" />
                </button>
              </div>
            )}
            {(!showProducts || pasoPago) && (
              <button
                onClick={handleEnterClick}
                disabled={pasoPago === 'seleccion' || pasoPago === 'tarjeta'}
                className={`w-full h-[72px] font-black rounded-[12px] text-[20px] tracking-widest mt-2 shadow-md transition-all ${
                  pasoPago === 'efectivo'
                    ? 'bg-[#2ECC71] text-white hover:bg-[#27ae60]'
                    : 'bg-[#2C3E50] text-white hover:bg-black'
                } disabled:opacity-50 disabled:cursor-not-allowed`}
              >
                {pasoPago === 'efectivo' ? 'CALCULAR CAMBIO' : 'ENTER'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function BotonesFinalizacion({ onFinalizar, h = 'h-[72px]' }) {
  return (
    <div className="flex w-full space-x-4">
      <button
        onClick={() => onFinalizar(false)}
        className={`flex-1 bg-[#2ECC71] text-white ${h} rounded-[16px] font-bold text-xl hover:bg-[#27ae60] shadow-sm`}
      >
        FINALIZAR
      </button>
      <button
        onClick={() => onFinalizar(true)}
        className={`flex-1 border-4 border-[#1976D2] text-[#1976D2] bg-white ${h} rounded-[16px] font-bold text-xl flex items-center justify-center hover:bg-blue-50 shadow-sm`}
      >
        <Printer className="mr-2" size={24} /> TICKET
      </button>
    </div>
  );
}