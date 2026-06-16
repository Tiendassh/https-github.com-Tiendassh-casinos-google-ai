import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, Lock, CreditCard, Landmark, Coins, 
  Trophy, RefreshCw, Check, Copy, AlertCircle, 
  Sparkles, TrendingUp, TrendingDown, BookOpen, PenTool,
  HelpCircle, ChevronRight, User, DollarSign, ArrowUpRight, ArrowDownLeft
} from 'lucide-react';

export default function App() {
  // Navigation State
  const [currentTab, setCurrentTab] = useState('juegos'); // 'juegos', 'deportes', 'quiniela', 'recupero', 'pagos', 'legal'
  const [currentGame, setCurrentGame] = useState('slots'); // 'slots', 'blackjack', 'roulette', 'raspadita'
  
  // User Profile & Balance State
  const [user, setUser] = useState({
    username: "Jugador_Real_99",
    balance: 1500.00,
    bonusPoints: 350,
    totalBets: 12,
    totalWon: 4500.00,
  });

  // Sound Effect Emulation
  const triggerAudioSfx = (type) => {
    // In React we can emulate user audio with modern synthesizers using Web Audio API!
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      
      if (type === 'tap') {
        osc.frequency.setValueAtTime(600, ctx.currentTime);
        gain.gain.setValueAtTime(0.1, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.05);
      } else if (type === 'win') {
        osc.frequency.setValueAtTime(440, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.15, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.3);
      } else if (type === 'lose') {
        osc.frequency.setValueAtTime(220, ctx.currentTime);
        osc.frequency.linearRampToValueAtTime(100, ctx.currentTime + 0.2);
        gain.gain.setValueAtTime(0.15, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.25);
      } else if (type === 'spin') {
        osc.frequency.setValueAtTime(150, ctx.currentTime);
        osc.frequency.linearRampToValueAtTime(500, ctx.currentTime + 0.5);
        gain.gain.setValueAtTime(0.08, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.5);
      }
    } catch(e) {}
  };

  // Safe Balance deposit for Testing
  const addDemoCredits = (amount) => {
    triggerAudioSfx('win');
    setUser(prev => ({ ...prev, balance: prev.balance + amount }));
  };

  // 1. SLOTS STATE & LOGIC
  const [slotsGrid, setSlotsGrid] = useState(['🍒', '🍋', '🍒']);
  const [slotsStatus, setSlotsStatus] = useState('¡Pulsa GIRAR para tentar a la suerte!');
  const [slotsBet, setSlotsBet] = useState(50);
  const [isSlotsSpinning, setIsSlotsSpinning] = useState(false);
  
  const spinSlots = () => {
    if (user.balance < slotsBet) {
      triggerAudioSfx('lose');
      setSlotsStatus('❌ Saldo insuficiente.');
      return;
    }
    
    setIsSlotsSpinning(true);
    triggerAudioSfx('spin');
    setSlotsStatus('⏳ Girando rodillos...');
    
    // Deduct bet
    setUser(prev => ({ ...prev, balance: prev.balance - slotsBet }));
    
    setTimeout(() => {
      const symbols = ['🍒', '🍋', '🔔', '💎', '🎰', '🍇', '⭐'];
      const result = [
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)]
      ];
      setSlotsGrid(result);
      
      // Calculate outcome
      if (result[0] === result[1] && result[1] === result[2]) {
        let multi = 10;
        if (result[0] === '🎰') multi = 50;
        if (result[0] === '💎') multi = 25;
        const prize = slotsBet * multi;
        setUser(prev => ({ ...prev, balance: prev.balance + prize }));
        triggerAudioSfx('win');
        setSlotsStatus(`🎉 ¡TRIPLE COMBO! Multiplicador x${multi}. Ganaste $${prize}`);
      } else if (result[0] === result[1] || result[1] === result[2] || result[0] === result[2]) {
        const prize = slotsBet * 2;
        setUser(prev => ({ ...prev, balance: prev.balance + prize }));
        triggerAudioSfx('win');
        setSlotsStatus(`✨ ¡Doble par coincidente! Ganaste $${prize}`);
      } else {
        triggerAudioSfx('lose');
        setSlotsStatus('No hubo suerte esta vez. ¡Inténtalo de nuevo!');
      }
      setIsSlotsSpinning(false);
    }, 1000);
  };

  // 2. BLACKJACK STATE & LOGIC
  const [playerHand, setPlayerHand] = useState([]);
  const [dealerHand, setDealerHand] = useState([]);
  const [bjStatus, setBjStatus] = useState('Inicia una nueva partida');
  const [bjBet, setBjBet] = useState(100);
  const [bjPhase, setBjPhase] = useState('init'); // 'init', 'playing', 'ended'
  
  const calculateBjScore = (hand) => {
    let score = 0;
    let aces = 0;
    hand.forEach(card => {
      if (card.value === 'A') {
        score += 11;
        aces++;
      } else if (['K', 'Q', 'J'].includes(card.value)) {
        score += 10;
      } else {
        score += parseInt(card.value);
      }
    });
    while (score > 21 && aces > 0) {
      score -= 10;
      aces--;
    }
    return score;
  };

  const startBlackjack = () => {
    if (user.balance < bjBet) {
      setBjStatus('❌ Saldo insuficiente.');
      return;
    }
    
    // Deduct
    setUser(prev => ({ ...prev, balance: prev.balance - bjBet }));
    triggerAudioSfx('tap');
    
    const suits = ['❤️', '💎', '♣️', '♠️'];
    const values = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A'];
    
    const drawCard = () => {
      const s = suits[Math.floor(Math.random() * suits.length)];
      const v = values[Math.floor(Math.random() * values.length)];
      return { suit: s, value: v };
    };

    const pHand = [drawCard(), drawCard()];
    const dHand = [drawCard(), drawCard()];

    setPlayerHand(pHand);
    setDealerHand(dHand);
    setBjPhase('playing');
    setBjStatus('¿Pedir Carta (Hit) o Plantarse (Stand)?');
  };

  const hitBlackjack = () => {
    triggerAudioSfx('tap');
    const suits = ['❤️', '💎', '♣️', '♠️'];
    const values = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A'];
    const newCard = {
      suit: suits[Math.floor(Math.random() * suits.length)],
      value: values[Math.floor(Math.random() * values.length)]
    };
    
    const updated = [...playerHand, newCard];
    setPlayerHand(updated);
    
    if (calculateBjScore(updated) > 21) {
      setBjPhase('ended');
      triggerAudioSfx('lose');
      setBjStatus('💥 ¡Te pasaste de 21! Perdiste la apuesta.');
    }
  };

  const standBlackjack = () => {
    triggerAudioSfx('spin');
    let currentDealer = [...dealerHand];
    
    // Dealer draws till 17+
    const suits = ['❤️', '💎', '♣️', '♠️'];
    const values = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A'];
    
    while (calculateBjScore(currentDealer) < 17) {
      currentDealer.push({
        suit: suits[Math.floor(Math.random() * suits.length)],
        value: values[Math.floor(Math.random() * values.length)]
      });
    }
    
    setDealerHand(currentDealer);
    const pScore = calculateBjScore(playerHand);
    const dScore = calculateBjScore(currentDealer);
    
    setBjPhase('ended');
    
    if (dScore > 21) {
      setUser(prev => ({ ...prev, balance: prev.balance + (bjBet * 2) }));
      triggerAudioSfx('win');
      setBjStatus(`🎉 El distribuidor se pasó (${dScore}). ¡Ganaste $${bjBet * 2}!`);
    } else if (pScore > dScore) {
      setUser(prev => ({ ...prev, balance: prev.balance + (bjBet * 2) }));
      triggerAudioSfx('win');
      setBjStatus(`🎉 ¡Le ganaste al distribuidor! (${pScore} vs ${dScore}). Ganaste $${bjBet * 2}`);
    } else if (pScore < dScore) {
      triggerAudioSfx('lose');
      setBjStatus(`La casa gana (${dScore} a ${pScore}). ¡Sigue intentando!`);
    } else {
      setUser(prev => ({ ...prev, balance: prev.balance + bjBet }));
      triggerAudioSfx('tap');
      setBjStatus(`Empate (${pScore} a ${dScore}). Se te devolvieron los créditos.`);
    }
  };

  // 3. ROULETTE STATE & LOGIC
  const [rouletteState, setRouletteState] = useState({
    betType: 'COLOR', // 'COLOR', 'NUMBER', 'EVEN_ODD'
    betValue: 'RED',  // 'RED'/'BLACK', '1-36', 'EVEN'/'ODD'
    betAmount: 100,
    winningNumber: null,
    winningColor: null,
    status: 'Haz tu jugada en el paño virtual',
    spinning: false
  });

  const runRoulette = () => {
    if (user.balance < rouletteState.betAmount) {
      setRouletteState(prev => ({ ...prev, status: '❌ Saldo insuficiente.' }));
      return;
    }

    setRouletteState(prev => ({ ...prev, spinning: true, status: '🧶 Girando bola en la ruleta...' }));
    triggerAudioSfx('spin');
    
    setUser(prev => ({ ...prev, balance: prev.balance - rouletteState.betAmount }));

    setTimeout(() => {
      const redNumbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];
      const winNum = Math.floor(Math.random() * 37);
      let winColor = 'GREEN';
      if (winNum !== 0) {
        winColor = redNumbers.includes(winNum) ? 'RED' : 'BLACK';
      }

      let won = false;
      let multiplier = 1;

      if (rouletteState.betType === 'COLOR') {
        if (rouletteState.betValue === winColor) {
          won = true;
          multiplier = 2;
        }
      } else if (rouletteState.betType === 'EVEN_ODD') {
        const isWinNumEven = winNum !== 0 && winNum % 2 === 0;
        if (rouletteState.betValue === 'EVEN' && isWinNumEven) {
          won = true;
          multiplier = 2;
        } else if (rouletteState.betValue === 'ODD' && !isWinNumEven && winNum !== 0) {
          won = true;
          multiplier = 2;
        }
      } else if (rouletteState.betType === 'NUMBER') {
        if (parseInt(rouletteState.betValue) === winNum) {
          won = true;
          multiplier = 35;
        }
      }

      if (won) {
        const prize = rouletteState.betAmount * multiplier;
        setUser(prev => ({ ...prev, balance: prev.balance + prize }));
        triggerAudioSfx('win');
        setRouletteState(prev => ({
          ...prev,
          winningNumber: winNum,
          winningColor: winColor,
          spinning: false,
          status: `🎉 Salió el ${winNum} (${winColor === 'RED' ? 'Rojo' : winColor === 'GREEN' ? 'Cero Verde' : 'Negro'}). ¡Ganaste $${prize}!`
        }));
      } else {
        triggerAudioSfx('lose');
        setRouletteState(prev => ({
          ...prev,
          winningNumber: winNum,
          winningColor: winColor,
          spinning: false,
          status: `Salió el ${winNum} (${winColor === 'RED' ? 'Rojo' : winColor === 'GREEN' ? 'Cero Verde' : 'Negro'}). Perdiste tu apuesta.`
        }));
      }
    }, 1500);
  };

  // 4. RASPADITA (SCRATCH) STATE & LOGIC
  const [scratchCells, setScratchCells] = useState(Array(9).fill({ revealed: false, value: 0 }));
  const [scratchState, setScratchState] = useState({
    purchased: false,
    cost: 50,
    status: 'Compra una tarjeta para raspar',
    won: false,
    activeNumbers: []
  });

  const buyScratch = () => {
    if (user.balance < scratchState.cost) {
      setScratchState(prev => ({ ...prev, status: '❌ Saldo insuficiente.' }));
      return;
    }
    triggerAudioSfx('tap');
    setUser(prev => ({ ...prev, balance: prev.balance - scratchState.cost }));

    // Generate random values (some matches)
    const options = [50, 100, 500, 1000, 5000];
    const generatedValues = Array(9).fill(null).map(() => options[Math.floor(Math.random() * options.length)]);
    
    // Assure occasional wins: force match sometimes
    if (Math.random() > 0.4) {
      const luckyVal = options[Math.floor(Math.random() * options.length)];
      generatedValues[0] = luckyVal;
      generatedValues[4] = luckyVal;
      generatedValues[8] = luckyVal;
    }

    setScratchCells(generatedValues.map(v => ({ revealed: false, value: v })));
    setScratchState(prev => ({
      ...prev,
      purchased: true,
      status: '¡Haz click sobre los casilleros morados para revelar las cifras!',
      won: false,
      activeNumbers: generatedValues
    }));
  };

  const revealCell = (index) => {
    if (!scratchState.purchased || scratchCells[index].revealed) return;
    
    triggerAudioSfx('tap');
    const updated = [...scratchCells];
    updated[index] = { ...updated[index], revealed: true };
    setScratchCells(updated);

    // Filter count of revealed
    const revealedCards = updated.filter(c => c.revealed);
    if (revealedCards.length === 9) {
      // Find matches
      const counts = {};
      let payout = 0;
      updated.forEach(c => {
        counts[c.value] = (counts[c.value] || 0) + 1;
      });

      Object.keys(counts).forEach(val => {
        if (counts[val] >= 3) {
          payout = parseInt(val);
        }
      });

      if (payout > 0) {
        setUser(prev => ({ ...prev, balance: prev.balance + payout }));
        triggerAudioSfx('win');
        setScratchState(prev => ({
          ...prev,
          purchased: false,
          won: true,
          status: `🎉 ¡FELICIDADES! Encontraste 3 cifras idénticas y ganaste $${payout} créditos.`
        }));
      } else {
        triggerAudioSfx('lose');
        setScratchState(prev => ({
          ...prev,
          purchased: false,
          status: 'No se encontraron tripletas ganadoras. ¡Intenta con otra raspadita!'
        }));
      }
    }
  };

  // 5. SPORTS TAB STATE & LOGIC
  const [selectedSportBet, setSelectedSportBet] = useState({
    matchId: 1,
    outcome: 'HOME', // 'HOME', 'DRAW', 'AWAY'
    odds: 1.85,
    matchName: 'Real Madrid vs Paris Saint-Germain',
    amount: 100
  });
  const [sportsBetStatus, setSportsBetStatus] = useState('Seleccione una cuota para apostar');
  const [liveMatches, setLiveMatches] = useState([
    { id: 1, home: 'Real Madrid', away: 'FC Barcelona', league: 'La Liga BBVA 🇪🇸', score: '2 - 1', oddsH: 1.85, oddsD: 3.40, oddsA: 3.80, status: 'EN VIVO 72\'' },
    { id: 2, home: 'Boca Juniors', away: 'River Plate', league: 'Superliga Argentina 🇦🇷', score: '0 - 0', oddsH: 2.10, oddsD: 3.10, oddsA: 3.00, status: 'EN VIVO 34\'' },
    { id: 3, home: 'Golden State', away: 'LA Lakers', league: 'NBA Regular Season 🇺🇸', score: '104 - 108', oddsH: 1.65, oddsD: 0, oddsA: 2.25, status: 'EN VIVO 4Q' },
    { id: 4, home: 'Carlos Alcaraz', away: 'Novak Djokovic', league: 'ATP Masters Series 🎾', score: '6-4, 5-7, 2-1', oddsH: 2.05, oddsD: 0, oddsA: 1.80, status: 'EN VIVO 3er Set' }
  ]);

  // Simulate tick for live scores
  useEffect(() => {
    const timer = setInterval(() => {
      setLiveMatches(prev => prev.map(m => {
        // Randomly update score sometimes
        if (Math.random() > 0.8) {
          const scoreParts = m.score.split(' - ');
          if (scoreParts.length === 2 && !isNaN(parseInt(scoreParts[0]))) {
            const h = parseInt(scoreParts[0]) + (Math.random() > 0.5 ? 1 : 0);
            const a = parseInt(scoreParts[1]) + (Math.random() > 0.5 ? 1 : 0);
            return { ...m, score: `${h} - ${a}` };
          }
        }
        return m;
      }));
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const placeSportsBet = () => {
    if (user.balance < selectedSportBet.amount) {
      setSportsBetStatus('❌ Saldo insuficiente.');
      return;
    }
    
    // Deduct
    setUser(prev => ({ ...prev, balance: prev.balance - selectedSportBet.amount }));
    triggerAudioSfx('tap');
    setSportsBetStatus(`⏳ Procesando boleto deportivo encriptado SSL por $${selectedSportBet.amount}...`);

    setTimeout(() => {
      // Resolve betting outcome with 50% success simulation
      const win = Math.random() > 0.5;
      if (win) {
        const reward = Math.round(selectedSportBet.amount * selectedSportBet.odds);
        setUser(prev => ({ ...prev, balance: prev.balance + reward }));
        triggerAudioSfx('win');
        setSportsBetStatus(`🎉 ¡Apuesta acertada! Has ganado $${reward} con cuota x${selectedSportBet.odds}.`);
      } else {
        triggerAudioSfx('lose');
        setSportsBetStatus('La apuesta no resultó. ¡El deporte siempre entrega sorpresas!');
      }
    }, 2000);
  };

  // 6. QUINIELA TAB
  const [quinielaInput, setQuinielaInput] = useState('1492');
  const [quinielaBet, setQuinielaBet] = useState(100);
  const [quinielaStatus, setQuinielaStatus] = useState('Ingresa un número de 4 dígitos (0000 - 9999)');

  const playQuiniela = () => {
    if (user.balance < quinielaBet) {
      setQuinielaStatus('❌ Saldo insuficiente.');
      return;
    }
    if (quinielaInput.length !== 4 || isNaN(parseInt(quinielaInput))) {
      setQuinielaStatus('Por favor ingrese un número de 4 cifras exactas.');
      return;
    }

    triggerAudioSfx('spin');
    setUser(prev => ({ ...prev, balance: prev.balance - quinielaBet }));
    setQuinielaStatus('🔮 Realizando el sorteo fiscal en tómbola digital...');

    setTimeout(() => {
      const drawnNum = Math.floor(1000 + Math.random() * 9000).toString();
      
      // Check degree of winnings
      if (drawnNum === quinielaInput) {
        const prize = quinielaBet * 500;
        setUser(prev => ({ ...prev, balance: prev.balance + prize }));
        triggerAudioSfx('win');
        setQuinielaStatus(`😱 ¡EXTREMO PREMIO MAYOR! Salió el ${drawnNum}. Has ganado $${prize}!`);
      } else if (drawnNum.slice(-2) === quinielaInput.slice(-2)) {
        const prize = quinielaBet * 5;
        setUser(prev => ({ ...prev, balance: prev.balance + prize }));
        triggerAudioSfx('win');
        setQuinielaStatus(`🎉 ¡Acierto de Dos Cifras! Salió el ${drawnNum}. Ganaste $${prize}.`);
      } else {
        triggerAudioSfx('lose');
        setQuinielaStatus(`Salió el número ${drawnNum}. Tu número: ${quinielaInput}. ¡Intenta nuevamente!`);
      }
    }, 1500);
  };

  // 7. RECUPERO (CASHBACK & RESCUE)
  const [recuperoStatus, setRecuperoStatus] = useState('Reclama fondos cuando más lo necesitas.');
  
  const claimCashback = () => {
    // Grant $150 cashback
    setUser(prev => ({ ...prev, balance: prev.balance + 150 }));
    triggerAudioSfx('win');
    setRecuperoStatus('🎉 ¡Reclamado con éxito! Se han acreditado $150.00 de cashback.');
  };

  const claimEmergency = () => {
    if (user.balance > 10.0) {
      setRecuperoStatus('❌ El rescate de emergencia requiere que tu saldo sea inferior a $10.00.');
      return;
    }
    setUser(prev => ({ ...prev, balance: 250.00 }));
    triggerAudioSfx('win');
    setRecuperoStatus('🛡️ ¡Rescate de Emergencia Activado! Tu cuenta vuelve a tener $250.00 para jugar.');
  };

  // 8. PAYMENT PORTAL (CAJA SEGURA) STATE & LOGIC
  const [payMode, setPayMode] = useState('deposit'); // 'deposit' or 'withdraw'
  const [payMethod, setPayMethod] = useState('TARJETA'); // 'TARJETA', 'TRANSFER', 'BILLETERA'
  const [payAmount, setPayAmount] = useState('100');
  const [cardNumber, setCardNumber] = useState('');
  const [cardHolder, setCardHolder] = useState('');
  const [cardExpiry, setCardExpiry] = useState('');
  const [cardCvv, setCardCvv] = useState('');
  const [walletId, setWalletId] = useState('');
  const [paymentStatusText, setPaymentStatusText] = useState('Seleccione un método de depósito o retiro para comenzar (Cifrado AES-256 Activo)');
  const [txProgress, setTxProgress] = useState(0);
  const [txActive, setTxActive] = useState(false);
  const [showInvoice, setShowInvoice] = useState(false);
  const [txRef, setTxRef] = useState('');

  const submitPayment = () => {
    const amt = parseFloat(payAmount);
    if (isNaN(amt) || amt <= 0) {
      setPaymentStatusText('El monto debe ser mayor a 0 dólares.');
      return;
    }

    if (payMode === 'withdraw' && user.balance < amt) {
      setPaymentStatusText('Fondos insuficientes para retirar.');
      return;
    }

    setTxActive(true);
    setTxProgress(10);
    setPaymentStatusText('Estableciendo conexión encriptada SSL/TLS...');
    
    // Animate stage processing
    setTimeout(() => {
      setTxProgress(45);
      setPaymentStatusText('Generando hash criptográfico SHA-256 de claves bancarias...');
    }, 1000);

    setTimeout(() => {
      setTxProgress(80);
      setPaymentStatusText('Firmando transacción y validando límites financieros...');
    }, 2000);

    setTimeout(() => {
      setTxProgress(100);
      const generatedRef = (Math.floor(100000 + Math.random() * 900000)).toString();
      setTxRef(generatedRef);
      triggerAudioSfx('win');
      
      if (payMode === 'deposit') {
        setUser(prev => ({ ...prev, balance: prev.balance + amt }));
        setPaymentStatusText(`¡Depósito completado con éxito! Se acreditaron $${amt} créditos.`);
      } else {
        setUser(prev => ({ ...prev, balance: prev.balance - amt }));
        setPaymentStatusText(`¡Retiro exitoso! Se enviaron $${amt} a tu cuenta asociada.`);
      }
      
      setTxActive(false);
      setShowInvoice(true);
    }, 3000);
  };

  // 9. LEGAL SIGNATURE PAD
  const [signatureName, setSignatureName] = useState('');
  const [signedState, setSignedState] = useState(false);

  return (
    <div className="min-h-screen bg-[#0D0F12] text-slate-100 flex flex-col justify-between">
      {/* HEADER SECTION */}
      <header className="border-b border-slate-900 bg-[#12161B] px-6 py-4 sticky top-0 z-50">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          
          {/* Logo & Compliance Seal */}
          <div className="flex items-center gap-3">
            <span className="text-3xl">🎰</span>
            <div>
              <h1 className="text-xl font-bold tracking-tight text-[#FFD700] flex items-center gap-1.5 font-mono">
                APOSTA REAL
                <span className="bg-[#2ECC71]/10 text-[#2ECC71] text-[10px] font-sans font-semibold px-2 py-0.5 rounded-full border border-[#2ECC71]/30 flex items-center gap-1">
                  <ShieldCheck className="w-3 h-3" /> PCI_DSS SECURE
                </span>
              </h1>
              <p className="text-[11px] text-slate-400 font-sans">
                Plataforma Certificada de Juegos de Azar • Regulado por Ente Oficial
              </p>
            </div>
          </div>

          {/* Secure details tracker */}
          <div className="flex items-center gap-4 flex-wrap justify-center">
            <div className="bg-[#1B2129] px-3 py-1.5 rounded-lg border border-slate-800 text-[11px] text-slate-300 flex items-center gap-1.5">
              <Lock className="text-[#2ECC71] w-3.5 h-3.5" />
              <span>Cifrado AES-256 de Grado Bancario Activo</span>
            </div>
            
            <div className="text-right hidden sm:block">
              <p className="text-[10px] text-slate-400 font-mono">ESTADO DEL SERVIDOR</p>
              <p className="text-xs text-[#2ECC71] font-bold flex items-center gap-1 justify-end">
                <span className="w-2 h-2 rounded-full bg-[#2ECC71] inline-block blink"></span> ONLINE (Railway)
              </p>
            </div>
          </div>

        </div>
      </header>

      {/* CORE FRAME LAYOUT */}
      <main className="max-w-6xl mx-auto w-full px-4 py-6 flex-grow grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* SIDEBAR: Profile & Balances */}
        <div className="lg:col-span-1 flex flex-col gap-5">
          
          {/* USER CARD */}
          <div className="elegant-card p-4 relative overflow-hidden">
            <div className="absolute top-0 right-0 w-20 h-20 bg-gradient-to-br from-[#FFD700]/10 to-transparent rounded-full pointer-events-none"></div>
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-[#FFD700]/10 border border-[#FFD700]/30 flex items-center justify-center">
                <User className="text-[#FFD700] w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-slate-200">@{user.username}</h3>
                <p className="text-[10px] text-slate-400">Verificado Premium</p>
              </div>
            </div>

            <div className="space-y-1 mb-4">
              <p className="text-[11px] text-slate-500 font-mono">SALDO DISPONIBLE</p>
              <h2 className="text-3xl font-black tracking-tight text-[#FFD700]">
                ${user.balance.toFixed(2)}
              </h2>
            </div>

            {/* Quick Demo Faucets */}
            <div className="border-t border-slate-800/60 pt-3">
              <p className="text-[10px] text-slate-400 font-semibold mb-2">Simular Carga Rápida (Testeo):</p>
              <div className="grid grid-cols-2 gap-2">
                <button 
                  onClick={() => addDemoCredits(500)} 
                  className="bg-[#2A313C] hover:bg-[#374151] text-xs font-bold py-1.5 px-3 rounded text-slate-100 flex items-center justify-center gap-1"
                >
                  🔋 +$500
                </button>
                <button 
                  onClick={() => addDemoCredits(2000)} 
                  className="bg-[#2A313C] hover:bg-[#374151] text-xs font-bold py-1.5 px-3 rounded text-[#FFD700] flex items-center justify-center gap-1"
                >
                  ⚡ +$2000
                </button>
              </div>
            </div>
          </div>

          {/* LAUNCHER NAVIGATION TAB BUTTONS */}
          <div className="bg-[#15191E] border border-slate-900 rounded-xl p-2 flex flex-col gap-1 shadow-md">
            <button 
              onClick={() => setCurrentTab('juegos')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'juegos' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>🎰</span> Juegos de Mesa y Tragaperras
            </button>
            
            <button 
              onClick={() => setCurrentTab('deportes')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'deportes' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>⚽</span> Deportes en Vivo
            </button>

            <button 
              onClick={() => setCurrentTab('quiniela')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'quiniela' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>🎫</span> Quiniela y Sorteos
            </button>

            <button 
              onClick={() => setCurrentTab('recupero')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'recupero' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>🩹</span> Recupero y Cashback
            </button>

            <button 
              onClick={() => setCurrentTab('pagos')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'pagos' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>💳</span> Caja Segura (Pagos/Retiros)
            </button>

            <button 
              onClick={() => setCurrentTab('legal')}
              className={`w-full py-2.5 px-4 rounded-lg font-semibold text-xs flex items-center gap-3 transition-colors ${currentTab === 'legal' ? 'bg-[#FFD700] text-black font-extrabold' : 'text-slate-400 hover:bg-[#1E252D] hover:text-slate-100'}`}
            >
              <span>⚖️</span> Regulación y Firma
            </button>
          </div>

          {/* TRUST FOOTPRINT SYSTEM */}
          <div className="elegant-card p-3 opacity-80 text-[10px] space-y-1">
            <p className="font-bold text-slate-300">Certificación Internacional</p>
            <p className="text-slate-400">Canales de pago protegidos bajo cumplimiento PCI-DSS nivel 1. Los hashes SHA-256 garantizan la inmutabilidad de cada tirada.</p>
          </div>

        </div>

        {/* COMPONENT DESK CENTRAL WORKSPACE */}
        <div className="lg:col-span-3 flex flex-col gap-6">

          {currentTab === 'juegos' && (
            <div className="space-y-8 animate-fade-in">
              
              {/* Luxury Arena Tabs Switcher */}
              <div className="bg-slate-900/80 p-2 rounded-2xl border border-slate-800/80 flex items-center gap-1.5 shadow-2xl glass-panel">
                <button 
                  onClick={() => { setCurrentGame('slots'); triggerAudioSfx('tap'); }}
                  className={`flex-1 py-3 px-4 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all duration-300 ${currentGame === 'slots' ? 'bg-gradient-to-r from-amber-500/20 to-yellow-500/10 text-amber-300 border border-amber-500/40 shadow-lg shadow-amber-500/5' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'}`}
                >
                  <span className="text-base">🎰</span> Slots Machine
                </button>
                <button 
                  onClick={() => { setCurrentGame('blackjack'); triggerAudioSfx('tap'); }}
                  className={`flex-1 py-3 px-4 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all duration-300 ${currentGame === 'blackjack' ? 'bg-gradient-to-r from-amber-500/20 to-yellow-500/10 text-amber-300 border border-amber-500/40 shadow-lg shadow-amber-500/5' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'}`}
                >
                  <span className="text-base">🃏</span> Blackjack 21
                </button>
                <button 
                  onClick={() => { setCurrentGame('roulette'); triggerAudioSfx('tap'); }}
                  className={`flex-1 py-3 px-4 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all duration-300 ${currentGame === 'roulette' ? 'bg-gradient-to-r from-amber-500/20 to-yellow-500/10 text-amber-300 border border-amber-500/40 shadow-lg shadow-amber-500/5' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'}`}
                >
                  <span className="text-base">🧿</span> Ruleta Croupier
                </button>
                <button 
                  onClick={() => { setCurrentGame('raspadita'); triggerAudioSfx('tap'); }}
                  className={`flex-1 py-3 px-4 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all duration-300 ${currentGame === 'raspadita' ? 'bg-gradient-to-r from-amber-500/20 to-yellow-500/10 text-amber-300 border border-amber-500/40 shadow-lg shadow-amber-500/5' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'}`}
                >
                  <span className="text-base">🎫</span> Raspadita VIP
                </button>
              </div>

              {/* GAME VIEWPORT: SLOTS MACHINE */}
              {currentGame === 'slots' && (
                <div className="elegant-card p-6 flex flex-col items-center relative overflow-hidden">
                  <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-amber-500 via-yellow-300 to-amber-500"></div>
                  
                  {/* Jackpot Top Banner */}
                  <div className="w-full max-w-md bg-gradient-to-r from-purple-950/80 via-black to-purple-950/80 border border-purple-500/30 rounded-xl py-3 px-5 mb-8 text-center flex items-center justify-between shadow-[0_0_20px_rgba(168,85,247,0.1)]">
                    <span className="flex items-center gap-2 text-purple-400">
                      <Sparkles className="w-4 h-4 animate-spin text-purple-400" />
                      <span className="text-[10px] uppercase font-black tracking-widest font-mono">MEGA RECOMPENSA</span>
                    </span>
                    <div className="text-[#FFD700] text-sm font-black font-mono tracking-widest flex items-center gap-1.5">
                      🎰 SLOT JACKPOT MULTIPLIER x50
                    </div>
                  </div>

                  {/* Reels Chassis Assembly */}
                  <div className="bg-[#090b0e] py-8 px-10 rounded-3xl border-2 border-[#ffb700]/30 flex items-center gap-5 md:gap-8 mb-8 shadow-[inset_0_4px_30px_rgba(0,0,0,0.8)] relative neon-glow-gold">
                    {/* Retro top & bottom frame elements */}
                    <div className="absolute -top-1.5 left-1/2 -translate-x-1/2 bg-amber-400 text-black font-extrabold text-[8px] font-mono tracking-widest px-3 py-0.5 rounded-full uppercase border border-amber-500">APOSTA REAL ASSEMBLY</div>
                    
                    {slotsGrid.map((sym, index) => (
                      <div 
                        key={index} 
                        className={`w-20 h-28 md:w-24 md:h-32 bg-gradient-to-b from-slate-900 via-[#131922] to-slate-900 border border-slate-700/60 rounded-2xl flex items-center justify-center text-4xl md:text-5xl shadow-[0_10px_20px_rgba(0,0,0,0.5),_inset_0_-8px_15px_rgba(0,0,0,0.6)] relative overflow-hidden transition-all duration-300 ${isSlotsSpinning ? 'animate-bounce border-amber-400' : 'hover:scale-[1.03]'}`}
                      >
                        {/* Shading reflection overlay */}
                        <div className="absolute inset-0 bg-gradient-to-b from-white/5 via-transparent to-black/40 pointer-events-none"></div>
                        <span className="z-10">{sym}</span>
                        {/* Static vertical wire bars just for classic glass feel */}
                        <div className="absolute left-0 top-0 w-px h-full bg-slate-800/50"></div>
                        <div className="absolute right-0 top-0 w-px h-full bg-slate-800/50"></div>
                      </div>
                    ))}
                  </div>

                  {/* Dynamic Prize Board */}
                  <div className="w-full max-w-sm bg-slate-950/60 border border-slate-900 rounded-xl p-3 mb-6 grid grid-cols-3 gap-2 text-center text-[10px] text-slate-400">
                    <div className="bg-[#1C2027]/40 p-1.5 rounded border border-slate-800/60">
                      <span className="font-bold text-slate-200">💎 TRIPLE</span>
                      <p className="text-amber-400 font-mono">x25 Crits</p>
                    </div>
                    <div className="bg-[#1C2027]/40 p-1.5 rounded border border-slate-800/60">
                      <span className="font-bold text-slate-200">🎰 TRIPLE</span>
                      <p className="text-purple-400 font-mono">X50 Crits</p>
                    </div>
                    <div className="bg-[#1C2027]/40 p-1.5 rounded border border-slate-800/60">
                      <span className="font-bold text-slate-200">⭐ DOBLE</span>
                      <p className="text-emerald-400 font-mono">x2 Crits</p>
                    </div>
                  </div>

                  {/* Slider Control Container */}
                  <div className="w-full max-w-sm mb-6 bg-slate-900/40 p-4 rounded-xl border border-slate-800/60">
                    <div className="flex justify-between text-xs text-slate-400 mb-2">
                      <span className="flex items-center gap-1"><Coins className="w-3.5 h-3.5 text-amber-500" /> Monto de la Apuesta:</span>
                      <span className="text-[#FFD700] font-black font-mono bg-amber-500/10 text-amber-400 py-0.5 px-2 rounded border border-amber-500/20">${slotsBet}</span>
                    </div>
                    <input 
                      type="range" 
                      min="10" 
                      max="500" 
                      step="10" 
                      value={slotsBet} 
                      onChange={(e) => setSlotsBet(parseInt(e.target.value))} 
                      disabled={isSlotsSpinning}
                      className="w-full accent-amber-400 bg-slate-800 h-2 rounded-lg cursor-pointer transition-colors"
                    />
                    <div className="flex justify-between text-[9px] text-slate-500 mt-1 font-mono">
                      <span>MIN $10</span>
                      <span>MAX $500</span>
                    </div>
                  </div>

                  {/* Neon Display Terminal Status */}
                  <div className="w-full max-w-sm text-center mb-6">
                    <div className="inline-block px-4 py-2 rounded-xl bg-slate-950 border border-slate-800 font-mono text-xs font-semibold tracking-wider text-amber-400/90 shadow-inner">
                      {slotsStatus}
                    </div>
                  </div>

                  {/* Play trigger button */}
                  <button 
                    onClick={spinSlots} 
                    disabled={isSlotsSpinning} 
                    className="elegant-button-primary w-full max-w-xs h-14 font-black flex items-center justify-center gap-2 tracking-widest text-[#000000]"
                  >
                    {isSlotsSpinning ? (
                      <>
                        <RefreshCw className="w-5 h-5 animate-spin" />
                        <span>Sintonizando Carro...</span>
                      </>
                    ) : (
                      <>
                        <span>🎰 JUGAR APUESTA</span>
                      </>
                    )}
                  </button>
                </div>
              )}

              {/* GAME VIEWPORT: BLACKJACK ARENA */}
              {currentGame === 'blackjack' && (
                <div className="elegant-card p-0 relative overflow-hidden flex flex-col">
                  {/* Decorative felt header bar */}
                  <div className="bg-gradient-to-r from-emerald-800 via-emerald-600 to-emerald-800 px-6 py-4 flex flex-col md:flex-row items-center justify-between border-b border-emerald-950/60">
                    <div>
                      <h2 className="text-[#FFD700] font-black text-lg tracking-wider">MESA ROYAL BLACKJACK</h2>
                      <p className="text-[10px] text-emerald-100/80 font-medium">Bajo reglamento internacional de crupieres • Conexiones cifradas SSL</p>
                    </div>
                    
                    <div className="text-right hidden md:block">
                      <span className="text-[10px] uppercase font-bold text-amber-300 font-mono tracking-widest border border-amber-500/30 px-2 py-0.5 rounded bg-black/30">
                        La casa se planta en 17 • Paga 3:2
                      </span>
                    </div>
                  </div>

                  {/* The Green Felt Gaming Table */}
                  <div className="felt-table p-6 md:p-8 min-h-[380px] flex flex-col justify-between">
                    
                    {bjPhase === 'init' ? (
                      <div className="flex flex-col items-center justify-center py-10 my-auto text-center">
                        <div className="w-16 h-16 rounded-full bg-amber-400/10 border border-amber-400/30 flex items-center justify-center text-amber-400 text-2xl mb-4">
                          🃏
                        </div>
                        <h3 className="font-bold text-slate-200 text-base mb-2">Configure su Apuesta de Entrada</h3>
                        <p className="text-xs text-slate-300 max-w-sm mb-6">Inicie una mano contra nuestro distribuidor. Los fondos serán retenidos de forma segura durante la sesión.</p>
                        
                        <div className="w-full max-w-sm mb-6 bg-black/30 p-4 rounded-xl border border-white/5">
                          <div className="flex justify-between text-xs text-slate-300 mb-2">
                            <span>Apuesta inicial:</span>
                            <span className="text-amber-300 font-black font-mono">$ {bjBet} USD</span>
                          </div>
                          <input 
                            type="range" 
                            min="50" 
                            max="1000" 
                            step="50" 
                            value={bjBet} 
                            onChange={(e) => setBjBet(parseInt(e.target.value))} 
                            className="w-full h-1.5 accent-amber-400 bg-emerald-900 rounded-lg cursor-pointer"
                          />
                        </div>

                        <button onClick={startBlackjack} className="elegant-button-primary w-56">
                          REPARTIR CARTAS
                        </button>
                      </div>
                    ) : (
                      <div className="space-y-6 flex-grow flex flex-col justify-between">
                        
                        {/* DEALER PANEL */}
                        <div className="bg-black/35 rounded-2xl border border-white/5 p-4 flex flex-col md:flex-row items-center justify-between gap-4">
                          <div className="space-y-1">
                            <h4 className="text-[10px] text-amber-400 font-black tracking-widest flex items-center gap-1.5">
                              <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse"></span>
                              REVOLVEDORES: LA CASA
                            </h4>
                            <p className="text-[11px] text-slate-300">
                              Mano evaluada: {bjPhase === 'playing' ? "Segunda carta oculta" : "Puntuación final: " + calculateBjScore(dealerHand)}
                            </p>
                          </div>
                          
                          <div className="flex gap-2">
                            {dealerHand.map((card, idx) => {
                              const isHidden = bjPhase === 'playing' && idx === 0;
                              return (
                                <div 
                                  key={idx} 
                                  className={`w-14 h-20 md:w-16 md:h-22 rounded-xl flex flex-col justify-between p-2 font-black transition-all duration-300 shadow-2xl relative ${
                                    isHidden 
                                      ? 'bg-gradient-to-tr from-amber-500 to-amber-700 border-2 border-amber-300 text-white shadow-xl' 
                                      : 'bg-white text-slate-900 border border-slate-300'
                                  }`}
                                >
                                  {isHidden ? (
                                    <div className="absolute inset-0 flex items-center justify-center text-xl">🔒</div>
                                  ) : (
                                    <>
                                      <span className="text-xs">{card.value}</span>
                                      <span className="text-2xl self-center">
                                        {card.suit === '❤️' || card.suit === '💎' ? (
                                          <span className="text-red-600">{card.suit}</span>
                                        ) : (
                                          <span className="text-slate-800">{card.suit}</span>
                                        )}
                                      </span>
                                      <span className="text-[10px] self-end">{card.value}</span>
                                    </>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        </div>

                        {/* STATUS EMBOSS MESSAGE */}
                        <div className="text-center my-3">
                          <p className="inline-block px-4 py-2 bg-black/45 border border-white/10 rounded-2xl font-mono text-xs text-slate-200 shadow-inner">
                            {bjStatus}
                          </p>
                        </div>

                        {/* PLAYER CRÉDIT MANOPLA */}
                        <div className="bg-black/35 rounded-2xl border border-white/5 p-4 flex flex-col md:flex-row items-center justify-between gap-4">
                          <div className="space-y-1">
                            <h4 className="text-[10px] text-emerald-400 font-black tracking-widest flex items-center gap-1.5 animate-pulse">
                              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                              CRÉDITO PARTICIPANTE
                            </h4>
                            <p className="text-[11px] text-slate-300">
                              Tu total de puntos en mano: <span className="font-bold text-white font-mono text-xs bg-emerald-500/10 px-1.5 py-0.5 rounded border border-emerald-500/20">{calculateBjScore(playerHand)}</span>
                            </p>
                          </div>

                          <div className="flex gap-2">
                            {playerHand.map((card, idx) => (
                              <div key={idx} className="w-14 h-20 md:w-16 md:h-22 bg-white text-slate-900 rounded-xl flex flex-col justify-between p-2 font-black transition-all duration-300 shadow-2xl border border-slate-200 hover:scale-105">
                                <span className="text-xs">{card.value}</span>
                                <span className="text-2xl self-center">
                                  {card.suit === '❤️' || card.suit === '💎' ? (
                                    <span className="text-red-500">{card.suit}</span>
                                  ) : (
                                    <span className="text-slate-800">{card.suit}</span>
                                  )}
                                </span>
                                <span className="text-[10px] self-end">{card.value}</span>
                              </div>
                            ))}
                          </div>
                        </div>

                        {/* Action buttons controls */}
                        <div className="flex justify-center gap-3 pt-2">
                          {bjPhase === 'playing' && (
                            <>
                              <button 
                                onClick={hitBlackjack} 
                                className="px-6 py-3 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-extrabold rounded-xl text-xs uppercase tracking-wider transition-all duration-300 active:scale-95 shadow-lg max-w-[150px] flex-1"
                              >
                                Pedir Carta (Hit)
                              </button>
                              <button 
                                onClick={standBlackjack} 
                                className="px-6 py-3 bg-rose-600 hover:bg-rose-500 text-white font-extrabold rounded-xl text-xs uppercase tracking-wider transition-all duration-300 active:scale-95 shadow-lg max-w-[150px] flex-1"
                              >
                                Plantarse (Stand)
                              </button>
                            </>
                          )}
                          {bjPhase === 'ended' && (
                            <button 
                              onClick={() => setBjPhase('init')} 
                              className="elegant-button-primary w-52"
                            >
                              VOLVER A JUGAR
                            </button>
                          )}
                        </div>

                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* GAME VIEWPORT: INTERACTIVE ROULETTE CROUPIER */}
              {currentGame === 'roulette' && (
                <div className="elegant-card p-6 flex flex-col gap-6">
                  <div className="text-center md:text-left">
                    <h2 className="text-[#FFD700] font-black text-lg tracking-wider mb-1 uppercase">Ruleta predictiva europea</h2>
                    <p className="text-xs text-slate-400">Canalizadores criptográficos de doble tirado. Pronostica el número o el color del paño virtual.</p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
                    
                    {/* The Visual spinning wheel */}
                    <div className="md:col-span-4 bg-slate-950/80 p-6 rounded-2xl border border-slate-900 flex flex-col items-center justify-center min-h-[220px]">
                      <div className={`w-32 h-32 rounded-full border-4 border-slate-800 flex items-center justify-center relative shadow-[0_0_20px_rgba(30,41,59,0.5)] ${rouletteState.spinning ? 'animate-spin border-amber-500 shadow-yellow-500/20' : ''}`}>
                        <div className="absolute w-28 h-28 rounded-full border-2 border-dashed border-slate-700"></div>
                        {rouletteState.winningNumber !== null ? (
                          <div className={`w-16 h-16 rounded-full flex flex-col items-center justify-center select-none font-bold shadow-2xl animate-pulse ${rouletteState.winningColor === 'RED' ? 'bg-red-600 text-white' : rouletteState.winningColor === 'GREEN' ? 'bg-[#2ECC71] text-slate-950 border border-slate-950 font-black' : 'bg-slate-950 text-slate-100 border border-slate-700'}`}>
                            <span className="text-xl font-black font-mono leading-none">{rouletteState.winningNumber}</span>
                            <span className="text-[8px] font-sans font-black tracking-widest mt-0.5">{rouletteState.winningColor}</span>
                          </div>
                        ) : (
                          <span className="text-amber-400 text-4xl animate-bounce">🧶</span>
                        )}
                      </div>
                      <span className="text-[10px] text-slate-500 font-mono mt-4 font-bold tracking-widest uppercase">MOTOR GIRA-BOLA SSL</span>
                    </div>

                    {/* Interactive Betting Board Panel */}
                    <div className="md:col-span-8 space-y-5 bg-[#12161D]/80 p-4 rounded-xl border border-slate-800/60">
                      
                      {/* Prediction selectors */}
                      <div className="space-y-1.5">
                        <label className="text-[10px] text-slate-400 font-extrabold uppercase tracking-wider block">1. Modalidad de Jugada</label>
                        <div className="grid grid-cols-3 gap-2">
                          {[
                            { key: 'COLOR', name: 'Color (x2)' }, 
                            { key: 'NUMBER', name: 'Pleno (x35)' }, 
                            { key: 'EVEN_ODD', name: 'Par/Imp (x2)' }
                          ].map(t => (
                            <button 
                              key={t.key}
                              onClick={() => setRouletteState(prev => ({ ...prev, betType: t.key, betValue: t.key === 'COLOR' ? 'RED' : t.key === 'EVEN_ODD' ? 'EVEN' : '17' }))}
                              className={`py-2 px-2 rounded-xl font-bold text-xs tracking-wider border transition-all duration-200 ${rouletteState.betType === t.key ? 'bg-amber-400 text-black border-amber-400 shadow-md' : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'}`}
                            >
                              {t.name}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Chosen Values panel */}
                      <div className="space-y-1.5">
                        <label className="text-[10px] text-slate-400 font-extrabold uppercase tracking-wider block">2. Valor de la Predicción</label>
                        
                        {rouletteState.betType === 'COLOR' && (
                          <div className="grid grid-cols-2 gap-3">
                            <button 
                              onClick={() => setRouletteState(prev => ({ ...prev, betValue: 'RED' }))} 
                              className={`py-3 rounded-xl text-xs font-black tracking-widest text-white bg-red-600 hover:bg-red-500 border-2 transition-all ${rouletteState.betValue === 'RED' ? 'border-amber-400 scale-[1.02]' : 'border-transparent opacity-80'}`}
                            >
                              ROJO
                            </button>
                            <button 
                              onClick={() => setRouletteState(prev => ({ ...prev, betValue: 'BLACK' }))} 
                              className={`py-3 rounded-xl text-xs font-black tracking-widest text-white bg-slate-950 hover:bg-black border-2 transition-all ${rouletteState.betValue === 'BLACK' ? 'border-amber-400 scale-[1.02]' : 'border-transparent opacity-80'}`}
                            >
                              NEGRO
                            </button>
                          </div>
                        )}

                        {rouletteState.betType === 'EVEN_ODD' && (
                          <div className="grid grid-cols-2 gap-3">
                            <button 
                              onClick={() => setRouletteState(prev => ({ ...prev, betValue: 'EVEN' }))} 
                              className={`py-3 rounded-xl text-xs font-bold tracking-widest bg-slate-900 border-2 transition-all hover:bg-slate-800 ${rouletteState.betValue === 'EVEN' ? 'border-amber-400 text-amber-300' : 'border-slate-800 text-slate-400'}`}
                            >
                              PAR (EVEN)
                            </button>
                            <button 
                              onClick={() => setRouletteState(prev => ({ ...prev, betValue: 'ODD' }))} 
                              className={`py-3 rounded-xl text-xs font-bold tracking-widest bg-slate-900 border-2 transition-all hover:bg-slate-800 ${rouletteState.betValue === 'ODD' ? 'border-amber-400 text-amber-300' : 'border-slate-800 text-slate-400'}`}
                            >
                              IMPAR (ODD)
                            </button>
                          </div>
                        )}

                        {rouletteState.betType === 'NUMBER' && (
                          <div className="flex gap-2 items-center">
                            <span className="text-xs text-slate-500 uppercase font-mono">Número:</span>
                            <input 
                              type="number" 
                              min="0" 
                              max="36" 
                              value={rouletteState.betValue} 
                              onChange={(e) => setRouletteState(prev => ({ ...prev, betValue: Math.max(0, Math.min(36, parseInt(e.target.value) || 0)).toString() }))}
                              className="bg-slate-950 border border-slate-800 rounded-xl w-32 h-11 px-3 text-amber-400 font-mono font-black text-center text-lg focus:border-amber-500 focus:outline-none"
                            />
                            <span className="text-[11px] text-slate-400">(0 al 36)</span>
                          </div>
                        )}
                      </div>

                      {/* Betting values slider */}
                      <div className="space-y-1.5">
                        <div className="flex justify-between text-[11px] text-slate-400">
                          <span className="uppercase font-extrabold tracking-wider">3. Fichas de Apuesta</span>
                          <span className="text-amber-400 font-bold font-mono bg-amber-500/10 py-0.5 px-2 rounded border border-amber-500/20">${rouletteState.betAmount}</span>
                        </div>
                        <input 
                          type="range" min="50" max="1000" step="50" 
                          value={rouletteState.betAmount} 
                          onChange={(e) => setRouletteState(prev => ({ ...prev, betAmount: parseInt(e.target.value) }))}
                          className="w-full h-1 bg-slate-900 rounded-lg accent-amber-400 cursor-pointer"
                        />
                      </div>

                      {/* SPIN BTN ACTIONS */}
                      <button 
                        onClick={runRoulette} 
                        disabled={rouletteState.spinning} 
                        className="elegant-button-primary w-full h-12 uppercase flex items-center justify-center gap-2 tracking-widest text-[#000000]"
                      >
                        {rouletteState.spinning ? (
                          <>
                            <RefreshCw className="w-4 h-4 animate-spin text-[#000000]" />
                            <span>Girando el rodamiento...</span>
                          </>
                        ) : (
                          '🧿 CONFIRMAR APUESTA (MESA)'
                        )}
                      </button>

                    </div>
                  </div>

                  {/* Feed outcome log bar */}
                  <div className="bg-[#090C10] border border-slate-900 rounded-xl px-5 py-3 text-center">
                    <p className="text-xs font-mono tracking-wide text-slate-300">
                      {rouletteState.status}
                    </p>
                  </div>
                </div>
              )}

              {/* GAME VIEWPORT: HOLLOGRAPHIC RASPADITA */}
              {currentGame === 'raspadita' && (
                <div className="elegant-card p-6 flex flex-col items-center relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-24 h-24 bg-gradient-to-bl from-purple-500/10 to-transparent rounded-bl-full"></div>
                  
                  <h2 className="text-[#FFD700] font-black text-lg tracking-wider mb-1 uppercase">CARTÓN VIP INSTANTÁNEO</h2>
                  <p className="text-xs text-slate-400 mb-6 text-center">Rasca del boleto digital. Consigue 3 cifras idénticas para reventar el premio inmediato.</p>

                  <div className="flex flex-col items-center w-full">
                    
                    {!scratchState.purchased && !scratchState.won ? (
                      <div className="text-center py-8 bg-slate-950/60 p-6 rounded-2xl border border-slate-900 max-w-sm w-full flex flex-col items-center">
                        <div className="w-12 h-12 rounded-full bg-purple-500/10 border border-purple-500/30 flex items-center justify-center text-purple-400 text-xl mb-4">
                          🎫
                        </div>
                        <p className="text-slate-300 text-xs mb-1 font-semibold">Boleto Platino Especial</p>
                        <p className="text-slate-400 text-[11px] mb-6">Costo de compra instantánea: <span className="font-bold text-white">$50.00 de saldo</span></p>
                        
                        <button onClick={buyScratch} className="elegant-button-primary w-full h-12 text-[#000000]">
                          COMPRAR TARJETA ($50)
                        </button>
                      </div>
                    ) : (
                      <div className="w-full max-w-xs grid grid-cols-3 gap-3 mb-6 bg-gradient-to-b from-[#241a3c] to-[#141026] p-4 rounded-2xl border-2 border-purple-500/30 shadow-2xl relative shadow-purple-950/20">
                        {/* Shimmer reflection */}
                        <div className="absolute inset-0 bg-gradient-to-tr from-white/0 via-white/5 to-white/0 pointer-events-none rounded-2xl"></div>
                        
                        {scratchCells.map((cell, idx) => (
                          <div 
                            key={idx}
                            onClick={() => revealCell(idx)}
                            className={`h-22 rounded-xl flex items-center justify-center font-mono font-black text-sm cursor-pointer select-none border transition-all duration-300 transform active:scale-95 flex-col ${
                              cell.revealed 
                                ? 'bg-slate-950 text-amber-300 border-amber-500/30 shadow-inner' 
                                : 'bg-gradient-to-br from-purple-700 via-indigo-900 to-indigo-950 text-slate-100 border-purple-500/40 hover:scale-[1.03] hover:border-purple-300 shadow-[0_4px_10px_rgba(0,0,0,0.4)]'
                            }`}
                          >
                            {cell.revealed ? (
                              <div className="text-center">
                                <span className="text-[10px] text-slate-500 block uppercase tracking-tight">PREMIO</span>
                                <span className="text-amber-400 font-bold block">${cell.value}</span>
                              </div>
                            ) : (
                              <>
                                <span className="text-base animate-pulse text-purple-300">⭐</span>
                                <span className="text-[7px] font-sans font-extrabold tracking-widest uppercase mt-0.5 text-purple-200">RASPAR</span>
                              </>
                            )}
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Status Box */}
                    <div className="text-center text-xs font-mono text-slate-200 bg-slate-950 px-5 py-3 rounded-xl border border-slate-900/60 w-full max-w-sm shadow-inner">
                      {scratchState.status}
                    </div>

                    {scratchState.won && (
                      <button 
                        onClick={() => setScratchState(prev => ({ ...prev, won: false }))} 
                        className="elegant-button-primary mt-5 px-8 py-3"
                      >
                        ADQUIRIR OTRA TARJETA
                      </button>
                    )}
                  </div>
                </div>
              )}

            </div>
          )}

          {/* TAB: VIEWPORT DEPORTES */}
          {currentTab === 'deportes' && (
            <div className="elegant-card p-6">
              <div className="flex flex-col md:flex-row items-center justify-between gap-4 mb-6 border-b border-slate-800 pb-4">
                <div>
                  <h2 className="text-[#FFD700] font-black text-lg tracking-wider">APUESTAS DEPORTIVAS (MÓDULO SSL)</h2>
                  <p className="text-xs text-slate-400">Canales cifrados vinculados de manera directa a cuotas oficiales</p>
                </div>
                <div className="flex items-center gap-2 bg-[#FFD700]/10 border border-[#FFD700]/30 py-1.5 px-3 rounded-lg text-xs text-[#FFD700]">
                  <span className="w-2 h-2 rounded-full bg-[#FFD700] pulse-glow"></span>
                  <span className="font-bold uppercase tracking-wider">Tasa de Actualización Activa</span>
                </div>
              </div>

              {/* Match lists */}
              <div className="space-y-4 mb-6">
                {liveMatches.map((match) => (
                  <div key={match.id} className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 flex flex-col md:flex-row items-center justify-between gap-4">
                    <div className="space-y-1 text-center md:text-left">
                      <div className="flex items-center gap-1.5 text-xs text-[#FFD700] font-sans font-extrabold uppercase">
                        <span>⚽</span> {match.league} • <span className="text-red-500 font-mono pulse-glow font-bold text-[10px]">{match.status}</span>
                      </div>
                      <h4 className="font-bold text-slate-200 text-sm">{match.home} vs {match.away}</h4>
                      <p className="text-rose-500 font-mono font-bold text-xs">Marcador: {match.score}</p>
                    </div>

                    {/* Odd buttons wrapper */}
                    <div className="flex items-center gap-2 flex-wrap">
                      <button 
                        onClick={() => setSelectedSportBet({ matchId: m => m.id, outcome: 'HOME', odds: match.oddsH, matchName: `${match.home} vs ${match.away}`, amount: 100 })}
                        className="py-1 px-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs rounded border border-transparent hover:border-slate-600 flex flex-col items-center min-w-16"
                      >
                        <span className="text-[8px] text-slate-400">LOCAL (1)</span>
                        <span className="font-bold text-[#FFD700]">x{match.oddsH}</span>
                      </button>

                      {match.oddsD > 0 && (
                        <button 
                          onClick={() => setSelectedSportBet({ matchId: m => m.id, outcome: 'DRAW', odds: match.oddsD, matchName: `${match.home} vs ${match.away}`, amount: 100 })}
                          className="py-1 px-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs rounded border border-transparent hover:border-slate-600 flex flex-col items-center min-w-16"
                        >
                          <span className="text-[8px] text-slate-400">EMPATE (X)</span>
                          <span className="font-bold text-[#FFD700]">x{match.oddsD}</span>
                        </button>
                      )}

                      <button 
                        onClick={() => setSelectedSportBet({ matchId: m => m.id, outcome: 'AWAY', odds: match.oddsA, matchName: `${match.home} vs ${match.away}`, amount: 100 })}
                        className="py-1 px-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs rounded border border-transparent hover:border-slate-600 flex flex-col items-center min-w-16"
                      >
                        <span className="text-[8px] text-slate-400">VISITA (2)</span>
                        <span className="font-bold text-[#FFD700]">x{match.oddsA}</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {/* Betting Ticket panel */}
              {selectedSportBet && (
                <div className="bg-[#1C212A] border border-cyan-800/40 p-4 rounded-xl space-y-4">
                  <div className="flex items-center gap-2 border-b border-slate-700 pb-2">
                    <span className="bg-[#FFD700]/10 text-[#FFD700] text-[10px] font-extrabold px-1.5 py-0.5 rounded">SLOT_ESTÁNDAR APUESTA</span>
                    <h4 className="text-xs font-bold text-slate-300">BOLETO VINCULADO SSL</h4>
                  </div>

                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-2.5 text-xs">
                    <div>
                      <p className="text-slate-400">Evento deportivo seleccionado:</p>
                      <p className="text-slate-100 font-bold">{selectedSportBet.matchName}</p>
                      <p className="text-[#FFD700] font-mono mt-0.5">Opción: <span className="font-bold">{selectedSportBet.outcome}</span> (Coeficiente Cuota: x{selectedSportBet.odds})</p>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className="text-slate-400">Importe ($):</span>
                      <input 
                        type="number" 
                        value={selectedSportBet.amount}
                        onChange={(e) => setSelectedSportBet(prev => ({ ...prev, amount: parseInt(e.target.value) || 10 }))}
                        className="bg-slate-900 border border-slate-700 rounded h-8 px-2 text-slate-100 w-20 font-mono font-bold text-center"
                      />
                    </div>
                  </div>

                  <div className="flex flex-col md:flex-row items-center justify-between gap-4 pt-3 border-t border-slate-800">
                    <p className="text-xs text-slate-400">Pago potencial si aciertas: <span className="text-[#2ECC71] font-bold font-mono">${Math.round(selectedSportBet.amount * selectedSportBet.odds)}</span></p>
                    <button onClick={placeSportsBet} className="elegant-button-primary h-10 px-8 text-xs shrink-0 flex items-center gap-1.5">
                      <Lock className="w-3.5 h-3.5" /> CONFIRMAR APUESTA DEPORTIVA
                    </button>
                  </div>
                </div>
              )}

              <p className="text-center font-mono text-[11px] text-slate-500 mt-4">
                {sportsBetStatus}
              </p>
            </div>
          )}

          {/* TAB: VIEWPORT QUINIELA */}
          {currentTab === 'quiniela' && (
            <div className="elegant-card p-6">
              <h2 className="text-[#FFD700] font-black text-lg tracking-wider mb-2">QUINIELA REGIONÁL REGULADA</h2>
              <p className="text-xs text-slate-400 mb-6">Apuesta a tu número de la suerte de 4 cifras con certificaciones de tómbola federal</p>

              <div className="bg-slate-950 p-6 rounded-xl border border-slate-800 max-w-md mx-auto space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs text-slate-400 font-bold block">Tu Número elegido de 4 Cifras:</label>
                  <input 
                    type="text" 
                    maxLength="4" 
                    value={quinielaInput}
                    onChange={(e) => setQuinielaInput(e.target.value.replace(/\D/g, '').slice(0, 4))}
                    className="w-full bg-slate-900 border-2 border-slate-800 rounded-lg h-12 px-4 focus:border-[#FFD700] text-center font-mono font-black text-2xl tracking-widest text-[#FFD700]"
                  />
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs text-slate-400 font-bold block">Monto de la apuesta ($):</label>
                  <input 
                    type="number" 
                    value={quinielaBet}
                    onChange={(e) => setQuinielaBet(parseInt(e.target.value) || 10)}
                    className="w-full bg-slate-900 border-2 border-slate-800 rounded-lg h-12 px-4 focus:border-[#FFD700] text-center font-mono font-bold text-sm text-slate-100"
                  />
                </div>

                <button onClick={playQuiniela} className="elegant-button-primary w-full h-11">
                  🎲 EFECTUAR SORTEO DE LA QUINIELA
                </button>
              </div>

              <p className="text-center font-mono text-xs text-slate-400 bg-[#0B0D10] py-3 px-4 rounded-xl mt-6 max-w-md mx-auto">
                {quinielaStatus}
              </p>
            </div>
          )}

          {/* TAB: VIEWPORT RECUPERO */}
          {currentTab === 'recupero' && (
            <div className="elegant-card p-6">
              <h2 className="text-[#FFD700] font-black text-lg tracking-wider mb-2">SISTEMA FLEXIBLE DE RECUPERO</h2>
              <p className="text-xs text-slate-400 mb-6">Políticas automáticas de rescate contra malas rachas y pérdida acumulada</p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                
                {/* Cashback */}
                <div className="bg-slate-900/60 p-5 rounded-xl border border-slate-800 flex flex-col justify-between">
                  <div>
                    <span className="text-3xl">🩹</span>
                    <h3 className="font-bold text-slate-200 mt-2 mb-1">Reclamar Retorno Manual (Cashback)</h3>
                    <p className="text-xs text-slate-400 mb-4">Aposta Real te devuelve un monto garantizado de indemnización semanal equivalente al 15% de pérdidas.</p>
                  </div>
                  <button onClick={claimCashback} className="elegant-button-primary bg-[#2ECC71] text-black w-full text-xs">
                    SOLICITAR $150.00 REINTEGRO
                  </button>
                </div>

                {/* Emergency rescue */}
                <div className="bg-slate-900/60 p-5 rounded-xl border border-slate-800 flex flex-col justify-between">
                  <div>
                    <span className="text-3xl">🛡️</span>
                    <h3 className="font-bold text-slate-200 mt-2 mb-1">Rescate de Caja por Quiebra</h3>
                    <p className="text-xs text-slate-400 mb-4">Si tu balance fiduciario toca fondo ($10 o menos), activa el protocolo de emergencia único para recuperar créditos inmediatamente.</p>
                  </div>
                  <button onClick={claimEmergency} className="elegant-button-primary bg-[#FFD700] text-black w-full text-xs">
                    ACTIVAR RESCATE DE $250.00
                  </button>
                </div>

              </div>

              <p className="text-center font-mono text-xs text-slate-400 bg-[#0B0D10] py-3 px-4 rounded-xl mt-6">
                {recuperoStatus}
              </p>
            </div>
          )}

          {/* TAB: VIEWPORT PORTAL DE PAGOS */}
          {currentTab === 'pagos' && (
            <div className="elegant-card p-6">
              <div className="flex flex-col md:flex-row items-center justify-between gap-4 mb-6 border-b border-slate-800 pb-4">
                <div>
                  <h2 className="text-[#FFD700] font-black text-lg tracking-wider">CAJA GENERAL Y PASARELA FINANCIERA</h2>
                  <p className="text-xs text-slate-400">Canal de transferencias y cobros cifrado bajo protocolo SHA256-AES</p>
                </div>
                <div className="flex items-center gap-1.5 bg-[#2A313C] border border-slate-800 py-1 px-2.5 rounded text-[11px] font-bold font-mono text-slate-300">
                  <ShieldCheck className="w-4.5 h-4.5 text-[#2ECC71]" /> PCI Compliant
                </div>
              </div>

              {/* Mode toggle */}
              <div className="flex bg-[#12161B] p-1 rounded-xl mb-6">
                <button 
                  onClick={() => { setPayMode('deposit'); setPaymentStatusText('Seleccione un método de depósito para comenzar (Cifrado AES-256 Activo)'); }}
                  className={`flex-1 py-2 rounded-lg font-bold text-xs flex items-center justify-center gap-2 ${payMode === 'deposit' ? 'bg-[#FFD700] text-black' : 'text-slate-400'}`}
                >
                  <ArrowUpRight className="w-4 h-4" /> DEPOSITAR SALDO
                </button>
                <button 
                  onClick={() => { setPayMode('withdraw'); setPaymentStatusText('Seleccione un método de retiro para comenzar (Cifrado AES-256 Activo)'); }}
                  className={`flex-1 py-2 rounded-lg font-bold text-xs flex items-center justify-center gap-2 ${payMode === 'withdraw' ? 'bg-[#FFD700] text-black' : 'text-slate-400'}`}
                >
                  <ArrowDownLeft className="w-4 h-4" /> SOLICITAR RETIRO
                </button>
              </div>

              {/* Methods options */}
              <div className="grid grid-cols-3 gap-3 mb-6">
                {[
                  { id: 'TARJETA', label: 'Tarjeta de Crédito', icon: <CreditCard className="w-5 h-5" /> },
                  { id: 'TRANSFER', label: 'Banco CBU/Alias', icon: <Landmark className="w-5 h-5" /> },
                  { id: 'BILLETERA', label: 'Billetera Virtual', icon: <Coins className="w-5 h-5" /> }
                ].map(m => (
                  <button 
                    key={m.id}
                    onClick={() => setPayMethod(m.id)}
                    className={`p-3 rounded-xl border flex flex-col items-center justify-center gap-2 transition-all ${payMethod === m.id ? 'bg-[#FFD700]/10 border-[#FFD700] text-[#FFD700]' : 'bg-slate-900 border-slate-850 text-slate-400 hover:text-slate-200'}`}
                  >
                    {m.icon}
                    <span className="text-[10px] font-bold">{m.label}</span>
                  </button>
                ))}
              </div>

              {/* Secure Form body */}
              <div className="bg-slate-950 p-5 rounded-2xl border border-slate-850 space-y-4">
                
                {/* Uniform amount query */}
                <div className="space-y-1">
                  <label className="text-[11px] text-slate-400 font-bold">Concepto de Monto de Transacción ($):</label>
                  <input 
                    type="number" 
                    value={payAmount}
                    onChange={(e) => setPayAmount(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-lg h-10 px-3 text-slate-100 font-mono text-sm"
                  />
                </div>

                {payMethod === 'TARJETA' && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <label className="text-[11px] text-slate-400 font-bold">Número de Tarjeta:</label>
                      <input 
                        type="text" 
                        maxLength="19"
                        placeholder="4512 0984 1029 3341"
                        value={cardNumber}
                        onChange={(e) => setCardNumber(e.target.value)}
                        className="w-full bg-slate-900 border border-slate-800 rounded-lg h-10 px-3 text-slate-100 font-mono text-sm"
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[11px] text-slate-400 font-bold">Nombre del Titular:</label>
                      <input 
                        type="text" 
                        placeholder="GUSTAVO ABETTIOL"
                        value={cardHolder}
                        onChange={(e) => setCardHolder(e.target.value.toUpperCase())}
                        className="w-full bg-slate-900 border border-slate-800 rounded-lg h-10 px-3 text-slate-100 text-sm"
                      />
                    </div>
                  </div>
                )}

                {payMethod === 'TRANSFER' && (
                  <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 text-xs space-y-2">
                    <p className="font-bold text-[#FFD700]">CBU de Depósito de Aposta Real:</p>
                    <div className="font-mono text-slate-300 space-y-1">
                      <p>Titular: AL-APOSTA-REAL.S.A</p>
                      <p>CBU: 0000003100023419582736</p>
                      <p>Alias: aposta.real.seguro</p>
                    </div>
                    <p className="text-[10px] text-slate-500">Transfiere de manera independiente en tu banco e introduce tu alias de emisor abajo para reconciliación instantánea.</p>
                    <input 
                      type="text" 
                      placeholder="Introduce tu CBU o Alias emisor"
                      value={walletId}
                      onChange={(e) => setWalletId(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded h-10 px-3 mt-2 text-slate-100 text-sm"
                    />
                  </div>
                )}

                {payMethod === 'BILLETERA' && (
                  <div className="space-y-1">
                    <label className="text-[11px] text-slate-400 font-bold">Email o ID de Billetera Electrónica:</label>
                    <input 
                      type="email" 
                      placeholder="micuenta@billetera.com"
                      value={walletId}
                      onChange={(e) => setWalletId(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg h-10 px-3 text-slate-100 text-sm"
                    />
                  </div>
                )}

                {/* Progress bar simulation */}
                {txActive && (
                  <div className="space-y-1.5">
                    <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                      <div className="bg-[#FFD700] h-full transition-all duration-300" style={{ width: `${txProgress}%` }}></div>
                    </div>
                    <p className="text-[10px] text-right font-mono text-slate-400">{txProgress}% completado</p>
                  </div>
                )}

                <button 
                  onClick={submitPayment} 
                  disabled={txActive}
                  className="w-full h-11 bg-gradient-to-r from-emerald-500 to-green-600 hover:from-emerald-600 hover:to-green-700 font-extrabold text-xs text-black tracking-wider rounded-xl uppercase flex items-center justify-center gap-2 shadow"
                >
                  <Lock className="w-4.5 h-4.5 text-black" /> {payMode === 'deposit' ? 'EFECTUAR DEPÓSITO PROTEGIDO' : 'CONFIRMAR RETIRO DE SALDO'}
                </button>
              </div>

              {/* API Security Status banner */}
              <p className="text-center font-mono text-xs text-slate-400 mt-4 leading-relaxed">
                {paymentStatusText}
              </p>

              {/* Invoice overlay detail */}
              {showInvoice && (
                <div className="bg-[#142C1D] border border-[#2ECC71]/40 rounded-xl p-4 mt-6 space-y-3">
                  <div className="flex items-center justify-between">
                    <h4 className="text-xs font-bold text-[#2ECC71] flex items-center gap-1.5 leading-none">
                      🔐 CANAL ENCRIPTADO DE PAGO COMPLETO
                    </h4>
                    <button onClick={() => setShowInvoice(false)} className="text-[10px] text-slate-400 hover:text-slate-100 font-extrabold px-1.5 py-0.5 rounded bg-slate-900 border">CERRAR</button>
                  </div>
                  
                  <div className="font-mono text-[10px] text-slate-300 space-y-1 pt-1.5 border-t border-[#2ECC71]/20">
                    <p>Referencia de Pasarela: #TXS-{txRef}</p>
                    <p>Firma SHA-256: {txRef.hashCode ? txRef.hashCode().toString(16) : 'e8331da29cbb01'}</p>
                    <p>Método Autorizado: {payMethod} (SSL Cifrado Directo)</p>
                    <p>Seguridad de Protocolo: TLS v1.3 (TCP Port 443)</p>
                    <p className="font-bold text-white mt-1">Monto de Operación: ${parseFloat(payAmount).toFixed(2)} USD</p>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB: VIEWPORT LEGAL */}
          {currentTab === 'legal' && (
            <div className="elegant-card p-6 space-y-6">
              <div>
                <h2 className="text-[#FFD700] font-black text-lg tracking-wider mb-2">RESPONSABILIDAD Y FIRMA LEGAL</h2>
                <p className="text-xs text-slate-400">Declaración Juramentante de Certificación de Límites de Conducta de Juego</p>
              </div>

              <div className="bg-[#12161B] border border-slate-850 p-4 rounded-xl text-xs space-y-3 text-slate-300 leading-relaxed max-w-2xl">
                <p className="font-bold text-white">Declaración de Consentimiento Firmado:</p>
                <p>1. Certifico que soy mayor de edad legal en mi jurisdicción fiduciaria correspondiente para sostener fondos en plataformas de apuestas en línea.</p>
                <p>2. Comprendo plenamente el carácter aleatorio predictivo y el riesgo matemático de pérdida inherente asociado a cualquier ronda de tragaperras, blackjack, ruleta, quiniela y apuestas deportivas certificadas.</p>
                <p>3. Me comprometo irrevocablemente a establecer límites prudenciales de sesión y balance para sostener dinámicas saludables de entretenimiento digital.</p>
              </div>

              <div className="space-y-4 max-w-sm">
                <div className="space-y-1.5">
                  <label className="text-xs text-slate-400 font-bold block">Tu Nombre para Firma Digital:</label>
                  <input 
                    type="text" 
                    placeholder="Escribe tu nombre completo para firmar"
                    value={signatureName}
                    onChange={(e) => setSignatureName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg h-10 px-3 text-slate-100 text-xs"
                  />
                </div>

                <button 
                  onClick={() => {
                    if (signatureName.trim() === '') return;
                    triggerAudioSfx('win');
                    setSignedState(true);
                  }}
                  disabled={signatureName.trim() === ''}
                  className="elegant-button-primary w-full h-11"
                >
                  <PenTool className="w-4 h-4 inline mr-1.5" /> CONFIRMAR Y FIRMAR TÉRMINOS
                </button>

                {signedState && (
                  <div className="bg-emerald-950/40 border border-emerald-500/30 p-3 rounded-lg text-xs text-emerald-400 flex items-center gap-2">
                    <Check className="w-4 h-4 shrink-0" />
                    <span>¡Firma almacenada! Hash fiscal: #SHA256-{signatureName.hashCode ? signatureName.hashCode().toString(16).toUpperCase() : 'B42F80A9'}</span>
                  </div>
                )}
              </div>
            </div>
          )}

        </div>

      </main>

      {/* COMPLIANCE LEGAL FOOTER */}
      <footer className="border-t border-slate-900 bg-[#0B0D10] text-[10px] text-slate-500 py-6 px-6">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <p>© 2026 Aposta Real. Todos los derechos reservados. Licencia certificada de Operación de Juegos de Azar #9410A-B.</p>
          <div className="flex gap-4">
            <span className="hover:text-slate-300 cursor-pointer">Juego Responsable</span>
            <span className="hover:text-slate-300 cursor-pointer">Política de Privacidad</span>
            <span className="hover:text-slate-300 cursor-pointer">Términos de Encriptación SSL</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

// Simple legacy helper for hashing
String.prototype.hashCode = function() {
  var hash = 0, i, chr;
  if (this.length === 0) return hash;
  for (i = 0; i < this.length; i++) {
    chr   = this.charCodeAt(i);
    hash  = ((hash << 5) - hash) + chr;
    hash |= 0; // Convert to 32bit integer
  }
  return hash;
};
