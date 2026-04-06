import {ActivatedRouteSnapshot, CanActivate, Router} from "@angular/router";
import {AuthService} from "../services/auth/auth.service";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {

    const expectedRole = route.data['role'];

    const user = this.authService.getCurrentUser();

    if (!user) {
      this.router.navigate(['/login']);
      return false;
    }

    if (user.type !== expectedRole) {
      this.router.navigate(['/']);
      return false;
    }

    return true;
  }
}