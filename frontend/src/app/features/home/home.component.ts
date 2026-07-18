import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ChangeDetectorRef,
  NgZone,
  inject,
  HostListener,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: 'home.component.html',
  styleUrl: 'home.component.scss',
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {

  private cdr = inject(ChangeDetectorRef);
  private zone = inject(NgZone);

  // Navbar scroll state
  navScrolled = false;

  // Typed text
  typedText = '';
  private words = ['timeline', 'tasks', 'vault', 'subscriptions'];
  private wordIndex = 0;
  private charIndex = 0;
  private isDeleting = false;
  private typingTimeout!: ReturnType<typeof setTimeout>;

  // Ticker items (doubled for seamless loop)
  private _ticker = [
    'TIMELINE TRACKING',
    'TASK MANAGEMENT',
    'ENCRYPTED VAULT',
    'SUBSCRIPTION ALERTS',
    'FULL-STACK BUILT',
    'SPRING BOOT BACKEND',
    'ANGULAR FRONTEND',
    'DOCKER DEPLOYED',
  ];
  tickerDouble = [...this._ticker, ...this._ticker];

  // Feature steps (stacking cards)
  steps = [
    {
      num: '01',
      icon: 'pi-calendar',
      title: 'Timeline',
      body: 'See your entire life in a single chronological view. Every milestone, event, and plan — past, present, and future — always in context.',
      preview: { label: 'Events this month', value: '14', sub: '3 upcoming this week' },
    },
    {
      num: '02',
      icon: 'pi-check-square',
      title: 'Tasks',
      body: 'Capture every commitment. Set priorities, deadlines, and categories. Nothing falls through the cracks.',
      preview: { label: 'Active tasks', value: '12', sub: '4 due today · 8 pending' },
    },
    {
      num: '03',
      icon: 'pi-lock',
      title: 'Vault',
      body: 'End-to-end encrypted document storage powered by MinIO. Upload any file type and access it securely from anywhere.',
      preview: { label: 'Vault status', value: 'E2E', sub: 'Encrypted · 7 files stored' },
    },
    {
      num: '04',
      icon: 'pi-credit-card',
      title: 'Subscriptions',
      body: 'Track every recurring charge, see total monthly spend, and get alerts before renewals hit your account.',
      preview: { label: 'Monthly spend', value: '₹2,840', sub: '6 active subscriptions' },
    },
  ];

  // Tech marquee
  private _tech = [
    { name: 'Angular 17', color: '#dd0031' },
    { name: 'Spring Boot', color: '#6db33f' },
    { name: 'PostgreSQL', color: '#336791' },
    { name: 'MinIO', color: '#c7250a' },
    { name: 'Docker', color: '#2496ed' },
    { name: 'JWT Auth', color: '#0f172a' },
    { name: 'PrimeNG', color: '#4f46e5' },
    { name: 'TypeScript', color: '#3178c6' },
  ];
  techDouble = [...this._tech, ...this._tech];

  // Orbiting icons for hero
  orbitItems = [
    { icon: 'pi-calendar',    deg: 0,   bg: '#f0fdf4', color: '#16a34a' },
    { icon: 'pi-check-square',deg: 60,  bg: '#eff6ff', color: '#2563eb' },
    { icon: 'pi-lock',        deg: 120, bg: '#fdf4ff', color: '#9333ea' },
    { icon: 'pi-credit-card', deg: 180, bg: '#fff7ed', color: '#ea580c' },
    { icon: 'pi-chart-bar',   deg: 240, bg: '#f0fdfa', color: '#0d9488' },
    { icon: 'pi-bolt',        deg: 300, bg: '#fefce8', color: '#ca8a04' },
  ];

  // ─── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit() {
    this.startTypingEffect();
  }

  ngAfterViewInit() {
    this.initScrollReveal();
  }

  ngOnDestroy() {
    clearTimeout(this.typingTimeout);
  }

  // ─── Scroll ─────────────────────────────────────────────────────────────────

  @HostListener('window:scroll')
  onScroll() {
    this.navScrolled = window.scrollY > 40;
  }

  // ─── Typing ─────────────────────────────────────────────────────────────────

  private startTypingEffect() {
    this.zone.run(() => {
      const word  = this.words[this.wordIndex];
      const speed = this.isDeleting ? 65 : 110;

      if (!this.isDeleting) {
        this.typedText = word.substring(0, ++this.charIndex);
        this.cdr.markForCheck();
        if (this.charIndex === word.length) {
          this.typingTimeout = setTimeout(() => {
            this.isDeleting = true;
            this.startTypingEffect();
          }, 2000);
          return;
        }
      } else {
        this.typedText = word.substring(0, --this.charIndex);
        this.cdr.markForCheck();
        if (this.charIndex === 0) {
          this.isDeleting = false;
          this.wordIndex  = (this.wordIndex + 1) % this.words.length;
        }
      }
      this.typingTimeout = setTimeout(() => this.startTypingEffect(), speed);
    });
  }

  // ─── Scroll reveal ──────────────────────────────────────────────────────────

  private initScrollReveal() {
    const obs = new IntersectionObserver(
      entries => entries.forEach(e => {
        if (e.isIntersecting) e.target.classList.add('is-visible');
      }),
      { threshold: 0.06, rootMargin: '0px 0px -50px 0px' }
    );
    setTimeout(() =>
      document.querySelectorAll('.fade-in').forEach(el => obs.observe(el)),
      120
    );
  }
}