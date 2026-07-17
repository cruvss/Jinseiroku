import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink], // Required for navigation buttons
  templateUrl: 'home.component.html',
  styleUrl: 'home.component.scss' // Note: Angular 17+ uses styleUrl (singular)
})
export class HomeComponent { }