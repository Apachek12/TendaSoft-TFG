import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Delete, ImageIcon, ChevronLeft, ChevronRight, Trash2, Banknote, CreditCard, ArrowLeft, Printer, Monitor } from 'lucide-react';

export default function Caja() {
  const navigate = useNavigate();
  // --- ESTADOS LÓGICOS TPV ---
  const [showProducts, setShowProducts] = useState(false);
  const [ventaActual, setVentaActual] = useState([]);
  const [codigoInput, setCodigoInput] = useState('');
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [catSeleccionada, setCatSeleccionada] = useState('todas');
  const [usuarioNombre, setUsuarioNombre] = useState('Vendedor');

  // --- ESTADOS LÓGICOS COBRO ---
  const [pasoPago, setPasoPago] = useState(null);
  const [dineroEntregado, setDineroEntregado] = useState('');
  const [cambio, setCambio] = useState(null);

  const scrollCategoriasRef = useRef(null);
  const total = ventaActual.reduce((acc, item) => acc + (item.precio * item.cantidad), 0);

  // --- INTEGRACIÓN BACKEND ---
  useEffect(() => {
    // Obtener nombre del usuario para la cabecera
    const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));
    if (auth) setUsuarioNombre(auth.nombreUsuario || auth.nombreReal);

    const fetchData = async () => {
      try {
        const [resProd, resCat] = await Promise.all([
          axios.get('http://localhost:8080/api/productos'),
          axios.get('http://localhost:8080/api/categorias')
        ]);
        setCategorias(resCat.data);

        let productosCruzados = [];
        resCat.data.forEach(cat => {
          if (cat.productos) cat.productos.forEach(prod => productosCruzados.push({ ...prod, idCategoriaReal: cat.id }));
        });

        const productosFinales = resProd.data.map(p => {
          const prodEncontrado = productosCruzados.find(pc => pc.codigoBarras === p.codigoBarras);
          return { ...p, idCategoriaReal: prodEncontrado ? prodEncontrado.idCategoriaReal : 'sin-categoria' };
        });
        setProductos(productosFinales);
      } catch (e) { console.error("Error cargando TPV", e); }
    };
    fetchData();
  }, []);

  // --- LÓGICA DE VENTA ---
  const agregarProducto = (prod) => {
    setVentaActual(prev => {
      const existe = prev.find(item => item.codigoBarras === prod.codigoBarras);
      if (existe) return prev.map(item => item.codigoBarras === prod.codigoBarras ? { ...item, cantidad: item.cantidad + 1 } : item);
      return [...prev, { ...prod, cantidad: 1 }];
    });
    setCodigoInput('');
  };

  const eliminarProductoDeVenta = (codigoBarras) => {
    setVentaActual(prev => prev.filter(item => item.codigoBarras !== codigoBarras));
  };

  const desplazarCategorias = (direccion) => {
    if (scrollCategoriasRef.current) scrollCategoriasRef.current.scrollBy({ left: direccion === 'izq' ? -150 : 150, behavior: 'smooth' });
  };

  // --- LÓGICA DEL TECLADO INTELIGENTE ---
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
      else alert("Producto no encontrado");
    }
  };

  // --- LÓGICA DE COBRO ---
  const calcularCambio = () => {
    const entregado = parseFloat(dineroEntregado.replace(',', '.'));
    if (isNaN(entregado) || entregado < total) {
      alert("La cantidad entregada es menor que el total o es inválida.");
      return;
    }
    setCambio(entregado - total);
  };

  const finalizarVenta = async (imprimirTicket) => {
    if (ventaActual.length === 0) return;

    try {
      const auth = JSON.parse(localStorage.getItem('usuarioTendaSoft'));

      // 1. Construimos el JSON con la estructura que espera tu Backend
      const payloadVenta = {
        venta: {
          usuario: {
            idUsuario: parseInt(auth?.idUsuario || auth?.id)
          },
          metodoPago: pasoPago.toUpperCase(),
          // Si tu backend guarda total/cambio dentro de 'venta', añádelos aquí:
          total: parseFloat(total.toFixed(2)),
          dineroEntregado: pasoPago === 'efectivo'
            ? parseFloat(String(dineroEntregado).replace(',', '.'))
            : parseFloat(total.toFixed(2)),
          cambio: pasoPago === 'efectivo' ? parseFloat(cambio.toFixed(2)) : 0,
        },
        lineas: ventaActual.map(item => ({
          producto: {
            codigoBarras: String(item.codigoBarras)
          },
          cantidad: parseInt(item.cantidad)
        }))
      };


      await axios.post('http://localhost:8080/api/ventas', payloadVenta);

      alert("¡Venta registrada con éxito!");

      // Limpieza de estados
      setVentaActual([]);
      setPasoPago(null);
      setDineroEntregado('');
      setCambio(null);

    } catch (error) {
      console.error("Error al vender:", error.response?.data);
      alert("Error: " + (error.response?.data?.message || "Revisa la consola"));
    }
  };

  return (
    <div className="h-screen bg-[#F4F7F9] p-4 flex flex-col font-sans overflow-hidden antialiased">

      {/* === CABECERA === */}
      <div className="flex items-center justify-between mb-4 flex-shrink-0">
        <div className="flex items-center">
          <button
            onClick={() => navigate('/dashboard')}
            className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
            title="Volver al inicio"
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

      {/* === CONTENIDO TPV === */}
      <div className="flex flex-1 overflow-hidden">
        {/* === SECCIÓN IZQUIERDA (60%) === */}
        <div className="w-[60%] h-full pr-2">
          <div className="bg-white rounded-[16px] shadow-sm flex flex-col h-full overflow-hidden border-none relative">
            {!pasoPago ? (
              <>
                <div className="flex bg-[#F8F9FA] px-4 py-2.5 text-[11px] font-bold text-[#7F8C8D] tracking-wider uppercase items-center">
                  <div className="flex-[2.5]">PRODUCTO</div>
                  <div className="flex-1 text-right">CANT.</div>
                  <div className="flex-[1.2] text-right">PRECIO</div>
                  <div className="w-8"></div>
                </div>
                <div className="flex-1 overflow-y-auto p-2 scrollbar-thin">
                  {ventaActual.length === 0 ? (
                    <div className="h-full flex items-center justify-center text-[#95A5A6] italic text-sm">Esperando productos...</div>
                  ) : (
                    ventaActual.map(item => (
                      <div key={item.codigoBarras} className="flex px-4 py-3 border-b border-[#F4F7F9] items-center animate-in fade-in slide-in-from-left-2">
                        <div className="flex-[2.5]">
                          <p className="font-bold text-[#2C3E50] text-sm">{item.nombre}</p>
                          <p className="text-[10px] text-[#95A5A6]">{item.codigoBarras}</p>
                        </div>
                        <div className="flex-1 text-right font-bold text-[#2C3E50]">x{item.cantidad}</div>
                        <div className="flex-[1.2] text-right font-black text-[#2C3E50]">{(item.precio * item.cantidad).toFixed(2)}€</div>
                        <button onClick={() => eliminarProductoDeVenta(item.codigoBarras)} className="w-8 flex justify-end text-red-400 hover:text-red-600 ml-2">
                          <Trash2 size={18} />
                        </button>
                      </div>
                    ))
                  )}
                </div>
                <div className="h-[1px] bg-[#E0E6ED] w-full" />
                <div className="h-20 flex items-center px-6 justify-between bg-white">
                  <button
                    onClick={() => setPasoPago('seleccion')}
                    disabled={ventaActual.length === 0}
                    className="bg-[#2ECC71] text-white font-bold px-10 h-[56px] rounded-[12px] text-lg shadow-sm hover:bg-[#27ae60] active:scale-95 transition-all disabled:bg-slate-200 disabled:text-slate-400"
                  >
                    COBRAR
                  </button>
                  <div className="text-right">
                    <span className="text-[#2C3E50] text-[32px] font-black leading-none">{total.toFixed(2)} €</span>
                  </div>
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
                    <p className="text-[#1565C0] text-5xl font-black mb-10 text-center tracking-tight">TOTAL: {total.toFixed(2)} €</p>
                    <p className="text-[#7F8C8D] text-xl font-bold mb-6 uppercase tracking-wider">Selecciona método de pago</p>
                    <div className="flex w-full space-x-6">
                      <button onClick={() => setPasoPago('efectivo')} className="flex-1 bg-[#2ECC71] text-white rounded-[16px] h-[100px] flex items-center justify-center text-2xl font-bold hover:bg-[#27ae60] shadow-md">
                        <Banknote className="mr-3" size={36} /> Efectivo
                      </button>
                      <button onClick={() => setPasoPago('tarjeta')} className="flex-1 bg-[#1976D2] text-white rounded-[16px] h-[100px] flex items-center justify-center text-2xl font-bold hover:bg-[#1565C0] shadow-md">
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
                        <div className="flex w-full space-x-4">
                          <button onClick={() => finalizarVenta(false)} className="flex-1 bg-[#2ECC71] text-white h-[72px] rounded-[16px] font-bold text-xl hover:bg-[#27ae60]">FINALIZAR</button>
                          <button onClick={() => finalizarVenta(true)} className="flex-1 border-4 border-[#1976D2] text-[#1976D2] h-[72px] rounded-[16px] font-bold text-xl flex items-center justify-center hover:bg-blue-50"><Printer className="mr-2" size={24}/> TICKET</button>
                        </div>
                      </div>
                    )}
                  </div>
                )}
                {pasoPago === 'tarjeta' && (
                  <div className="flex flex-col items-center justify-center h-full px-16">
                    <p className="text-[#1976D2] text-[64px] font-black mb-16 tracking-tighter">{total.toFixed(2)} €</p>
                    <div className="flex w-full space-x-6">
                      <button onClick={() => finalizarVenta(false)} className="flex-1 bg-[#2ECC71] text-white h-[80px] rounded-[16px] font-bold text-xl hover:bg-[#27ae60]">FINALIZAR</button>
                      <button onClick={() => finalizarVenta(true)} className="flex-1 border-4 border-[#1976D2] text-[#1976D2] h-[80px] rounded-[16px] font-bold text-xl flex items-center justify-center hover:bg-blue-50"><Printer className="mr-2" size={24}/> TICKET</button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* === SECCIÓN DERECHA (40%) === */}
        <div className="w-[40%] h-full flex flex-col">
          {!pasoPago && (
            <div className="animate-in fade-in">
              <input type="text" placeholder="Código de barras" value={codigoInput} onChange={(e) => setCodigoInput(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleEnterClick()} className="w-full h-[48px] bg-white border border-gray-200 rounded-[8px] text-center text-[16px] font-bold text-[#2C3E50] focus:outline-none mb-2 shadow-sm" />
              <button onClick={() => setShowProducts(!showProducts)} className="w-full h-[44px] bg-[#2C3E50] text-white font-bold rounded-[8px] uppercase text-xs tracking-widest mb-3">
                {showProducts ? "OCULTAR PRODUCTOS" : "VER PRODUCTOS"}
              </button>
            </div>
          )}
          <div className="flex-1 flex flex-col overflow-hidden">
            {showProducts && !pasoPago ? (
              <div className="flex flex-col h-full animate-in fade-in duration-200">
                <div className="flex items-center space-x-1 mb-3 w-full">
                  <button onClick={() => desplazarCategorias('izq')} className="p-1"><ChevronLeft size={24} /></button>
                  <div ref={scrollCategoriasRef} className="flex overflow-x-auto space-x-2 flex-1 scrollbar-hide">
                    <button onClick={() => setCatSeleccionada('todas')} className={`px-4 py-2 rounded-[6px] text-[12px] font-bold whitespace-nowrap ${catSeleccionada === 'todas' ? 'bg-[#2C3E50] text-white' : 'bg-white text-[#2C3E50]'}`}>Todos</button>
                    {categorias.map(cat => (
                      <button key={cat.id} onClick={() => setCatSeleccionada(cat.id)} className={`px-4 py-2 rounded-[6px] text-[12px] font-bold whitespace-nowrap ${String(catSeleccionada) === String(cat.id) ? 'bg-[#2C3E50] text-white' : 'bg-white text-[#2C3E50]'}`}>{cat.nombre}</button>
                    ))}
                  </div>
                  <button onClick={() => desplazarCategorias('der')} className="p-1"><ChevronRight size={24} /></button>
                </div>
                <div className="flex-1 overflow-y-auto grid grid-cols-3 gap-3 content-start pb-2">
                  {productos.filter(p => catSeleccionada === 'todas' || String(p.idCategoriaReal) === String(catSeleccionada)).map(p => (
                    <div
                      key={p.codigoBarras}
                      onClick={() => agregarProducto(p)}
                      className="bg-white rounded-[12px] p-2 flex flex-col items-center text-center cursor-pointer active:scale-95 shadow-sm hover:shadow-md transition-all border border-slate-100"
                    >
                      {/* CONTENEDOR DE LA IMAGEN */}
                      <div className="w-16 h-16 mb-2 rounded-lg overflow-hidden bg-slate-50 flex items-center justify-center shrink-0">
                        {p.urlImagen ? (
                          <img
                            src={`http://localhost:8080/uploads/${p.urlImagen}`}
                            alt={p.nombre}
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              // Si la imagen no carga, mostramos el icono por defecto
                              e.target.onerror = null;
                              e.target.style.display = 'none'; // Ocultamos la etiqueta img rota
                              e.target.nextSibling.style.display = 'block'; // Mostramos el ImageIcon
                            }}
                          />
                        ) : null}

                        {/* Icono de respaldo (Se muestra si no hay urlImagen, o si la carga falla) */}
                        <ImageIcon
                          size={24}
                          className="text-slate-300"
                          style={{ display: p.urlImagen ? 'none' : 'block' }}
                        />
                      </div>

                      {/* TEXTOS DEL PRODUCTO */}
                      <p className="text-[11px] font-bold text-[#2C3E50] truncate w-full leading-tight">{p.nombre}</p>
                      <p className="text-[14px] font-black text-[#1976D2] mt-1">{p.precio.toFixed(2)} €</p>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className={`grid grid-cols-3 gap-1.5 flex-1 pb-2 ${pasoPago ? 'pt-2' : ''}`}>
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, ',', 0].map(n => (
                  <button key={n} onClick={() => handleNumpadClick(n)} className="bg-white rounded-[12px] text-[28px] font-bold text-[#2C3E50] active:bg-slate-200 shadow-sm border border-gray-100">{n}</button>
                ))}
                <button onClick={handleNumpadDelete} className="bg-white rounded-[12px] flex items-center justify-center border border-gray-100"><Delete size={32} className="text-[#95A5A6]" /></button>
              </div>
            )}
            {(!showProducts || pasoPago) && (
              <button onClick={handleEnterClick} disabled={pasoPago === 'seleccion' || pasoPago === 'tarjeta'} className={`w-full h-[72px] font-black rounded-[12px] text-[20px] tracking-widest mt-2 shadow-md transition-all ${pasoPago === 'efectivo' ? 'bg-[#2ECC71] text-white' : 'bg-[#2C3E50] text-white'}`}>
                {pasoPago === 'efectivo' ? 'CALCULAR CAMBIO' : 'ENTER'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}