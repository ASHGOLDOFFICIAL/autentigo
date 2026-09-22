{
  description = "autentigo dev environment";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils = {
      url = "github:numtide/flake-utils";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            just
            libargon2
            openssl
            podman
            podman-compose
            sbt
            temurin-bin-21
          ];

          shellHook = ''
            export LD_LIBRARY_PATH="${pkgs.libargon2}/lib''${LD_LIBRARY_PATH:+:}$LD_LIBRARY_PATH"

            podman_sock="''${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/podman/podman.sock"
            if [ -S "$podman_sock" ]; then
              export DOCKER_HOST="unix://$podman_sock"
            fi
          '';
        };
      }
    );
}
