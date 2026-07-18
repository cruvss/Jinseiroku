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



  // Feature steps (stacking cards)
  steps = [
    {
      num: '01',
      icon: '/timeline.png',
      title: 'Timeline',
      body: 'See your entire life in a single chronological view. Every milestone, event, and plan — past, present, and future — always in context.',
      preview: { label: 'Events this month', sub: '3 upcoming this week', value: '14' }
    },
    {
      num: '02',
      icon: '/task.png',
      title: 'Tasks',
      body: 'Capture every commitment. Set priorities, deadlines, and categories. Nothing falls through the cracks.',
      preview: { label: 'Active tasks', sub: '4 due today · 8 pending', value: '12' }
    },
    {
      num: '03',
      icon: '/vault.png',
      title: 'Vault',
      body: 'Store your most sensitive files, passwords, and documents with client-side end-to-end encryption.',
      preview: { label: 'Secure items', sub: 'Encrypted locally', value: '48' }
    },
    {
      num: '04',
      icon: '/subscription.png',
      title: 'Subscriptions',
      body: 'Track recurring expenses, get renewal reminders, and see exactly where your money goes every month.',
      preview: { label: 'Monthly spend', sub: 'Next billing in 4 days', value: '$84' }
    }
  ];



  // Orbiting image icons for hero
  orbitItems = [
    { image: '/timeline.png',     deg: 0 },
    { image: '/task.png',         deg: 72 },
    { image: '/vault.png',        deg: 144 },
    { image: '/subscription.png', deg: 216 },
    { image: '/reminder.png',     deg: 288 },
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

  onScroll(event: Event) {
    const target = event.target as HTMLElement;
    this.navScrolled = target.scrollTop > 40;
  }

  scrollToTop(event: Event) {
    event.preventDefault();
    const page = document.querySelector('.page');
    if (page) {
      page.scrollTo({ top: 0, behavior: 'smooth' });
    }
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