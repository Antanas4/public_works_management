import {Component, HostListener, inject} from '@angular/core';
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false
})
export class HeaderComponent {

  #authService = inject(AuthService);
  readonly #router = inject(Router);

  lastScrollTop = 0;
  isNavbarHidden = false;

  get isAuthenticated() {
    return this.#authService.isAuthenticated();
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
