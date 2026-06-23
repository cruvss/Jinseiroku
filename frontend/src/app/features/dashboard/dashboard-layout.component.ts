import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { map } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule
  ],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <!-- Sidebar -->
      <mat-sidenav #sidenav mode="side" opened class="sidebar">
        <div class="sidebar-header">
          <h2 class="gradient-text">Jensiroku</h2>
          <p class="user-email">{{ userEmail$ | async }}</p>
        </div>
        
        <mat-nav-list>
          <a mat-list-item routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
            <mat-icon >dashboard</mat-icon>
            <span>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/inbox" routerLinkActive="active">
            <mat-icon class="dark-icon" >inbox</mat-icon>
            <span>Inbox</span>
          </a>
          <a mat-list-item routerLink="/vault" routerLinkActive="active">
            <mat-icon>lock</mat-icon>
            <span>Encrypted Vault</span>
          </a>
          <a mat-list-item routerLink="/subscriptions" routerLinkActive="active">
            <mat-icon>payments</mat-icon>
            <span>Subscriptions</span>
          </a>
          <a mat-list-item routerLink="/tasks" routerLinkActive="active">
            <mat-icon>task</mat-icon>
            <span>Tasks</span>
          </a>
          <a mat-list-item routerLink="/timeline" routerLinkActive="active">
            <mat-icon>timeline</mat-icon>
            <span>Life Timeline</span>
          </a>
        </mat-nav-list>
        
        <div class="sidebar-footer">
          <button mat-button color="warn" (click)="logout()" class="logout-btn">
            <mat-icon>logout</mat-icon>
            Logout
          </button>
        </div>
      </mat-sidenav>
      
      <!-- Main Content -->
      <mat-sidenav-content>
        <mat-toolbar color="primary" class="topbar">
          <button mat-icon-button (click)="sidenav.toggle()" class="menu-toggle">
            <mat-icon>menu</mat-icon>
          </button>
          <span class="spacer"></span>
          <span class="user-info">
            <mat-icon>person</mat-icon>
            {{ userEmail$ | async }}
          </span>
        </mat-toolbar>
        
        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`

    .sidenav-container {
      height: 100vh;
      
      .sidebar {
        width: 280px;
        background: #1a1a2e;
        border-right: 1px solid rgba(255, 255, 255, 0.1);
        
        .sidebar-header {
          padding: 1.5rem 1rem;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);
          
          h2 {
            font-size: 1.5rem;
            margin: 0;
            background: linear-gradient(135deg, #6366f1, #a855f7);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
          }
          
          .user-email {
            color: rgba(255, 255, 255, 0.6);
            font-size: 0.8rem;
            margin-top: 0.25rem;
          }
        }
        
        mat-nav-list {
          padding-top: 0.5rem;
          
          a {
            margin: 0.25rem 0.5rem;
            border-radius: 8px;
            
            mat-icon {
              margin-right: 0.75rem;
               color: rgba(0, 0, 0, 0.87);
            }
            
            &.active {
              background: rgba(99, 102, 241, 0.15);
              
              mat-icon {
                color: #6366f1;
              }
              
              span {
                color: #6366f1;
                font-weight: 500;
              }
            }
            
            &:hover {
              background: rgba(255, 255, 255, 0.05);
            }
          }
        }
        
        .sidebar-footer {
          position: absolute;
          bottom: 1rem;
          left: 0;
          right: 0;
          padding: 0 0.5rem;
          
          .logout-btn {
            width: 100%;
            justify-content: flex-start;
            
            mat-icon {
              margin-right: 0.75rem;
            }
          }
        }
      }
      
      .topbar {
        position: sticky;
        top: 0;
        z-index: 1000;
        background: rgba(26, 26, 46, 0.95);
        backdrop-filter: blur(10px);
        
        .menu-toggle {
          display: none;
        }
        
        .spacer {
          flex: 1;
        }
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.9rem;
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      .content {
        padding: 2rem;
        background: #121212;
        min-height: calc(100vh - 64px);
      }
    }
    
    @media (max-width: 768px) {
      .sidenav-container {
        .sidebar {
          width: 280px;
        }
        
        .topbar {
          .menu-toggle {
            display: block;
          }
        }
        
        .content {
          padding: 1rem;
        }
      }
    }
  `]
})
export class DashboardLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  userEmail$ = this.authService.currentUser$.pipe(
    map(user => user?.email || 'user@vaultlife.local')
  );
  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}