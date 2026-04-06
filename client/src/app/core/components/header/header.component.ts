import {Component, HostListener, inject, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";
import {User} from "../../models/user.model";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false
})
export class HeaderComponent implements  OnInit{

  #authService = inject(AuthService);
  readonly #router = inject(Router);

  user: User | null = null;
  lastScrollTop = 0;
  isNavbarHidden = false;

  get isAuthenticated() {
    return this.#authService.isAuthenticated();
  }

  ngOnInit() {
    this.#authService.user$.subscribe(user => {
      this.user = user;
    });
  }

  logout() {
    this.#authService.logout().subscribe(() => {
      this.#router.navigate(['/login']);
    });
  }

  @HostListener('window:scroll')
  onWindowScroll() {
    const currentScroll =
      window.pageYOffset || document.documentElement.scrollTop;

    this.isNavbarHidden =
      currentScroll > this.lastScrollTop && currentScroll > 50;

    this.lastScrollTop = currentScroll <= 0 ? 0 : currentScroll;
  }
}
