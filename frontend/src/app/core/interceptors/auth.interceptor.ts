import { HttpInterceptorFn } from "@angular/common/http";
import { AuthService } from "../services/auth.service";
import { inject } from "@angular/core";
import { Router } from "@angular/router";

export const authInerceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const token = authService.token;


    if (token){
        req = req.clone({setHeaders:{Authorization: ``}})
    }


}